package com.tf.services;

import com.tf.exception.CustomException;
import com.tf.exception.InvalidInputException;
import com.tf.exception.UnAuthorizedException;
import com.tf.exception.UploadFailedException;
import com.tf.models.*;
import com.tf.repositories.FileChunkMetadataRepository;
import com.tf.repositories.FileMetaDataRepository;
import com.tf.repositories.UserAccessRepository;
import com.tf.repositories.UserRepository;
import com.tf.util.InputHelper;
import com.tf.util.LocalFileSystemHelper;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.util.Pair;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;


@Service
@AllArgsConstructor
public class FileService {
    private final FileMetaDataRepository fileMetaDataRepository;
    private final FileChunkMetadataRepository fileChunkMetadataRepository;
    private final LocalFileSystemHelper localFileSystemHelper;
    private final UserAccessRepository userAccessRepository;
    private final UserRepository userRepository;

    public ResponseEntity<FileMetadataModel> uploadFile(MultipartFile file, String fileName, String fileHash, Authentication authentication) {
        User user = getUser(authentication);
        FileMetadataModel payload = InputHelper.sanitizeAndBuildMetadata(file, fileName, fileHash);
        fileName = payload.getFileName();
        String baseName = FilenameUtils.getBaseName(fileName);
        String extension = FilenameUtils.getExtension(fileName);
        List<String> fileNamesStartingWithFileName = userAccessRepository.findAllFilesWithSimilarNameAccessibleByUser(baseName, user);
        fileName = localFileSystemHelper.getNextAvailableFileNameIfAAlreadyExists(fileNamesStartingWithFileName,
                baseName, extension);

        payload.setFileName(fileName);
        payload.setSize(file.getSize());
        payload.setType(file.getContentType());
        payload.setStoredInChunks(true);
        payload = fileMetaDataRepository.save(payload);

        UserAccess userAccess = UserAccess.builder()
                .user(user)
                .fileMetadataModel(payload)
                .privilege(UserAccess.Privilege.OWNER)
                .build();
        userAccessRepository.save(userAccess);

        List<FileChunkMetadata> fileChunkMetadataList = localFileSystemHelper.storeFile(file, fileName, payload);
        fileChunkMetadataRepository.saveAll(fileChunkMetadataList);

        return new ResponseEntity<>(payload, HttpStatus.CREATED);
    }

    public ResponseEntity<ByteArrayResource> downloadFile(String fileId, HttpHeaders headers,
                                                          Authentication authentication) {
        FileMetadataModel metadata = checkUserAccessAndGetMetadata(fileId, authentication, UserAccess.Privilege.READER);
        List<FileChunkMetadata> chunkInfoList = getFileChunkMetadata(fileId);

        long fileSize = metadata.getSize();
        String contentRangeHeader = headers.getFirst(HttpHeaders.RANGE);
        List<Pair<Long, Long>> ranges = StringUtils.hasText(contentRangeHeader)
                ? InputHelper.validateAndParseByteRange(contentRangeHeader, fileSize)
                : Collections.emptyList();

        byte[] requestedData;
        HttpHeaders responseHeaders = new HttpHeaders();

        HttpStatus status = HttpStatus.PARTIAL_CONTENT;
        if (ranges.isEmpty()) {
            requestedData = localFileSystemHelper.getBytesForRange(chunkInfoList, 0, fileSize - 1);
            responseHeaders.setContentDisposition(ContentDisposition.parse("attachment; filename=\"" + metadata.getFileName() + "\""));
            responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            status = HttpStatus.OK;
        } else if (ranges.size() == 1) {
            Pair<Long, Long> range = ranges.get(0);
            requestedData = localFileSystemHelper.getBytesForRange(chunkInfoList, range.getFirst(), range.getSecond());
            setResponseHeaders(responseHeaders, range.getFirst(), range.getSecond(), fileSize, requestedData);
        } else {
            try {
                requestedData = localFileSystemHelper.buildMultipartResponse(ranges, chunkInfoList, fileSize);
            } catch (IOException e) {
                throw new CustomException("Unable to read data from the disk", e);
            }
            String MULTIPART_BOUNDARY = "--MULTIPART-BYTE-RANGE-BOUNDARY--";
            headers.setContentType(MediaType.valueOf("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY));
        }

        return new ResponseEntity<>(new ByteArrayResource(requestedData), responseHeaders, status);
    }

/*
    public ResponseEntity<ByteArrayResource> downloadFile(String fileId, Authentication authentication) {
        FileMetadataModel fileMetadata = checkUserAccessAndGetMetadata(fileId, authentication);
        List<FileChunkMetadata> chunkInfoList = getFileChunkMetadata(fileId);

        ByteBuffer buffer = ByteBuffer.allocate((int) fileMetadata.getSize());
        try {
            for (FileChunkMetadata chunkMetadata : chunkInfoList) {
                buffer.put(Files.readAllBytes(Paths.get(chunkMetadata.getPath())));
            }
        } catch (IOException e) {
            throw new CustomException("Failed to read the file", e);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileMetadata.getFileName() + "\"")
                .body(new ByteArrayResource(buffer.array()));
    }
*/

    public ResponseEntity<FileMetadataModel> updateFile(String fileId, MultipartFile file, String fileHash, String fileName, Authentication authentication) {
        if (file == null && !StringUtils.hasText(fileName)) {
            throw new InvalidInputException("At least one of the following should be provided to update: file, fileName");
        }

        if (file == null && StringUtils.hasText(fileHash)) {
            throw new InvalidInputException("FileHash cannot not be updated without updating file");
        }

        FileMetadataModel metadata = checkUserAccessAndGetMetadata(fileId, authentication, UserAccess.Privilege.EDITOR);
        fileName = InputHelper.validateFileName(file, fileName);
        List<String> allFilesWithExactNameAccessibleByUser = userAccessRepository.findAllFilesWithExactNameAccessibleByUser(fileName, getUser(authentication));
        if (!metadata.getFileName().equals(fileName) && !allFilesWithExactNameAccessibleByUser.isEmpty()) {
            throw new UploadFailedException("File with the same name already exists");
        }

        List<FileChunkMetadata> oldChunksToBeDeleted = List.of();
        boolean isFileUpdated = false;
        if (file != null) {
            fileHash = InputHelper.validateFileHash(file, fileHash);
            if (!metadata.getFileHash().equals(fileHash)) {
                oldChunksToBeDeleted = getFileChunkMetadata(fileId);
                List<FileChunkMetadata> fileChunkMetadataList = localFileSystemHelper.storeFile(file, fileName, metadata);
                fileChunkMetadataRepository.saveAll(fileChunkMetadataList);
                metadata.setFileHash(fileHash);
                metadata.setSize(file.getSize());
                metadata.setType(file.getContentType());
                isFileUpdated = true;
            }
        }

        metadata.setFileName(fileName);
        metadata.setStoredInChunks(false);
        fileMetaDataRepository.save(metadata);

        if (isFileUpdated) {
            localFileSystemHelper.deleteChunks(oldChunksToBeDeleted);
            fileChunkMetadataRepository.deleteAll(oldChunksToBeDeleted);
        }

        return new ResponseEntity<>(metadata, HttpStatus.OK);
    }

    public ResponseEntity<Object> deleteFile(String fileId, Authentication authentication) {
        FileMetadataModel metadata = checkUserAccessAndGetMetadata(fileId, authentication, UserAccess.Privilege.OWNER);
        List<FileChunkMetadata> fileChunkMetadataList = getFileChunkMetadata(fileId);

        localFileSystemHelper.deleteChunks(fileChunkMetadataList);
        fileChunkMetadataRepository.deleteAll(fileChunkMetadataList);
        userAccessRepository.deleteAllByFileId(UUID.fromString(fileId));
        fileMetaDataRepository.delete(metadata);

        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<List<FileMetadataModel>> listAllFiles(Authentication authentication) {
        List<FileMetadataModel> allFiles = userAccessRepository.findAllFilesByUser(getUser(authentication));
        //fileMetaDataRepository.findAllByUserId(getUserId(authentication));
        return new ResponseEntity<>(allFiles, HttpStatus.OK);
    }

    private List<FileChunkMetadata> getFileChunkMetadata(String fileId) {
        List<FileChunkMetadata> chunkInfoList = fileChunkMetadataRepository.findAllByFileIDOrderBySequenceNumberAsc(UUID.fromString(fileId));
        if (chunkInfoList == null || chunkInfoList.isEmpty()) {
            throw new CustomException("File not found or not uploaded in chunks");
        }
        return chunkInfoList;
    }

    private FileMetadataModel checkUserAccessAndGetMetadata(String fileId, Authentication authentication,
                                                            UserAccess.Privilege privilege) {
        FileMetadataModel metadata;
        try {
            UUID fileUUID = UUID.fromString(fileId);
            metadata = fileMetaDataRepository.getReferenceById(fileUUID);
            UserAccess userAccess = userAccessRepository.getUserAccessForTheFileToTheUser(fileUUID,
                    getUserId(authentication), privilege);
            if (userAccess == null) {
                throw new UnAuthorizedException();
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Please provide a valid file ID", e);
        } catch (EntityNotFoundException e) {
            throw new InvalidInputException("File not found for the file Id :" + fileId, e);
        }
        return metadata;
    }

    private User getUser(Authentication auth) {
        String userId = ((org.springframework.security.oauth2.jwt.Jwt) auth.getPrincipal()).getClaimAsString("userId");
        return new User(Long.parseLong(userId));
    }

    private Long getUserId(Authentication auth) {
        return getUser(auth).getUser_id();
    }

    private static void setResponseHeaders(HttpHeaders responseHeaders, long startByte, long endByte, long fileSize, byte[] requestedData) {
        responseHeaders.set(HttpHeaders.CONTENT_RANGE, String.format("bytes %d-%d/%d", startByte, endByte, fileSize));
        responseHeaders.setContentLength(requestedData.length);
        responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    }

    public ResponseEntity<Map<String, String>> initFileUpload(Authentication authentication) {
        HashMap<String, String> uniqueFileId = new HashMap<>();
        uniqueFileId.put("file_id", UUID.randomUUID().toString());
        return new ResponseEntity<>(uniqueFileId, HttpStatus.OK);
    }

    public ResponseEntity<Map<String, String>> uploadFileChunk(MultipartFile file, String fileHash, String fileId, int chunkNumber, Authentication authentication) {
        localFileSystemHelper.saveFileChunk(file, fileId, chunkNumber);
        return null;
    }

    public ResponseEntity<Map<String, String>> finalizeFileChunkUpload(String fileId, String fileName, String fileHash) {
        localFileSystemHelper.collateChunksAndSaveAsFile(fileId, fileName, fileHash);
        return null;
    }

    public ResponseEntity<FileMetadataModel> shareFile(String fileId, PermissionRequest request, Authentication authentication) {
        FileMetadataModel fileMetadataModel = fileMetaDataRepository.findById(UUID.fromString(fileId)).orElse(null);
        if (fileMetadataModel == null) {
            throw new CustomException("No Such file exists");
        }
        UserAccess userAccess = userAccessRepository.getUserAccessForTheFileToTheUser(UUID.fromString(fileId),
                getUserId(authentication), UserAccess.Privilege.OWNER);
        if (userAccess == null) {
            throw new UnAuthorizedException();
        }

        User user = userRepository.findById(request.getUserId()).orElse(null);
        if (user == null) {
            throw new CustomException("No Such user exists");
        }

        UserAccess userAccess1 = UserAccess.builder()
                .privilege(UserAccess.Privilege.valueOf(request.getPrivilege()))
                .user(user)
                .fileMetadataModel(fileMetadataModel)
                .build();
        userAccessRepository.save(userAccess1);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
