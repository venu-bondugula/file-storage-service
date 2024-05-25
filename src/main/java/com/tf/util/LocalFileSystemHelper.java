package com.tf.util;

import com.tf.exception.CustomException;
import com.tf.exception.UploadFailedException;
import com.tf.models.FileChunkMetadata;
import com.tf.models.FileMetadataModel;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Component
public class LocalFileSystemHelper {
    @Value("${tf.upload-dir}")
    private String uploadDir;

    @Value("${tf.chunk-size}")
    private long CHUNK_SIZE;

    @Value("${tf.store-in-chunks}")
    private boolean isStoredInChunks;

    private final String MULTIPART_BOUNDARY = "--MULTIPART-BYTE-RANGE-BOUNDARY--";

    public List<FileChunkMetadata> storeFile(MultipartFile file, String fileName, FileMetadataModel payload) {
        int chunkCount = (int) Math.ceil((double) file.getSize() / CHUNK_SIZE);
        long chunkSize = CHUNK_SIZE;
        if (!isStoredInChunks) {
            chunkCount = 1;
            chunkSize = file.getSize();
        }
        List<FileChunkMetadata> fileChunkMetadataList = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream()) {
            fileName = payload.getFile_id() + "-" + fileName;
            for (int i = 0; i < chunkCount; i++) {
                byte[] chunkData = new byte[(int) chunkSize];
                int bytesRead = Math.max(inputStream.read(chunkData), 0);
                Path chunkFilePath = Paths.get(uploadDir, fileName, UUID.randomUUID() + "-chunk-" + i);
                FileUtils.writeByteArrayToFile(new File(chunkFilePath.toUri()), chunkData);
                FileChunkMetadata chunkMetadata = new FileChunkMetadata(payload, i, chunkFilePath.toString(), bytesRead);
                fileChunkMetadataList.add(chunkMetadata);
            }
        } catch (IOException e) {
            throw new UploadFailedException("Error uploading file", e);
        }
        return fileChunkMetadataList;
    }

    public String getNextAvailableFileNameIfAAlreadyExists(List<String> fileNamesStartingWithFileName,
                                                           String baseName, String extension) {
        String fileName;
        HashSet<String> fileNames = new HashSet<>(fileNamesStartingWithFileName);

        int counter = 0;
        String uniqueFilename = baseName + "." + extension;

        while (fileNames.contains(uniqueFilename)) {
            counter++;
            uniqueFilename = baseName + " (" + counter + ")" + "." + extension;
        }

        fileName = uniqueFilename;
        return fileName;
    }

    public void deleteChunks(List<FileChunkMetadata> fileChunkMetadataList) {
        for (FileChunkMetadata chunkMetadata : fileChunkMetadataList) {
            File chunk = new File(uploadDir + File.separator + chunkMetadata.getPath());
            FileUtils.deleteQuietly(chunk);
        }
    }

    public byte[] buildMultipartResponse(List<Pair<Long, Long>> ranges, List<FileChunkMetadata> chunkInfoList,
                                         long fileSize) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(System.lineSeparator().getBytes());
        for (Pair<Long, Long> r : ranges) {
            byte[] partData = getBytesForRange(chunkInfoList, r.getFirst(), r.getSecond());
            String rangeBoundary = MULTIPART_BOUNDARY + System.lineSeparator() +
                    "Content-Type: application/octet-stream" + System.lineSeparator() +
                    "Content-Range: bytes " + r.getFirst() + "-" + r.getSecond() + "/" + fileSize + System.lineSeparator();
            byteArrayOutputStream.write(rangeBoundary.getBytes());
            byteArrayOutputStream.write(partData);
        }
        byteArrayOutputStream.write((System.lineSeparator() + MULTIPART_BOUNDARY + System.lineSeparator()).getBytes());
        return byteArrayOutputStream.toByteArray();
    }

    public byte[] getBytesForRange(List<FileChunkMetadata> chunkInfoList, long startByte, long endByte) {
        ByteBuffer buffer = ByteBuffer.allocate((int) (endByte - startByte + 1));

        int chunkIndex = 0;
        long currentChunkStart = 0;
        long currentChunkEnd;
        while (currentChunkStart <= endByte) {
            currentChunkStart = CHUNK_SIZE * chunkIndex;
            currentChunkEnd = currentChunkStart + CHUNK_SIZE - 1;

            // Check if this chunk contributes to the requested byte range
            if (currentChunkEnd >= startByte && currentChunkStart <= endByte) {
                FileChunkMetadata chunkMetadata = chunkInfoList.get(chunkIndex);
                String chunkPath = chunkMetadata.getPath();
                byte[] chunkData;
                try {
                    chunkData = FileUtils.readFileToByteArray(new File(chunkPath));
                } catch (IOException e) {
                    throw new CustomException("Unable to read data from the disk", e);
                }
                long readFrom = 0;
                long chunkToRead = CHUNK_SIZE;

                if (currentChunkStart < startByte) {
                    readFrom = Math.abs(startByte - currentChunkStart);
                    chunkToRead -= readFrom;
                }
                if (endByte < currentChunkEnd)
                    chunkToRead -= Math.abs(endByte - currentChunkEnd);

                buffer.put(chunkData, (int) readFrom, (int) chunkToRead);
            }
            chunkIndex++;
        }

        return buffer.array();
    }

    public void saveFileChunk(MultipartFile file, String fileId, int chunkNumber) {
        String filePath = uploadDir + "/temp" + "/" + fileId + "/" + chunkNumber;
        try (InputStream inputStream = file.getInputStream()) {
            byte[] chunkData = inputStream.readAllBytes();
            FileUtils.writeByteArrayToFile(new File(filePath), chunkData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void collateChunksAndSaveAsFile(String fileId, String fileName, String fileHash) {
        String filePath = uploadDir + "/temp" + "/" + fileId;

        File fileLocation = new File(filePath);
        File[] files = fileLocation.listFiles();

        if (files == null) {
            return;
        }

        Arrays.sort(files, Comparator.comparing(file -> Integer.parseInt(file.getName())));
        File fullFile = new File("/complete/" + fileName);
        for (File file : files) {
            try {
                FileUtils.writeByteArrayToFile(fullFile, FileUtils.readFileToByteArray(file), true);
            } catch (IOException e) {
                throw new RuntimeException(e);

            }
        }
    }

}
