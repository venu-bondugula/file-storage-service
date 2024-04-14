package com.tf.util;

import com.tf.exception.*;
import com.tf.models.FileMetadataModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class InputHelper {
    @Value("${tf.file-hash-algorithm}")
    private static String fileHashAlgorithm;

    public static FileMetadataModel sanitizeAndBuildMetadata(MultipartFile file, String fileName, String fileHash) {
        if (file == null) {
            throw new FileNotProvidedException();
        }
        fileName = validateFileName(file, fileName);
        fileHash = validateFileHash(file, fileHash);

        return FileMetadataModel.Builder.builder()
                .fileName(fileName)
                .fileHash(fileHash)
                .size(file.getSize())
                .type(file.getContentType())
                .build();
    }

    public static String validateFileHash(MultipartFile file, String fileHash) {
        String calculatedFileHash = calculateFileHash(file);
        if (!StringUtils.hasText(fileHash)) {
            return calculatedFileHash;
        }

        if (!calculatedFileHash.equalsIgnoreCase(fileHash)) {
            throw new FileHashMismatchException();
        }
        return calculatedFileHash;
    }

    private static String calculateFileHash(MultipartFile file) {
        String hash;
        try {
            hash = "SHA-256".equals(fileHashAlgorithm) ? calculateSha256Hash(file) : calculateMD5Hash(file);
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new CustomException("Failed to read data from the file input", e);
        }
        return hash;
    }

    private static String calculateMD5Hash(MultipartFile file) throws IOException {
        return DigestUtils.md5DigestAsHex(file.getBytes());
    }

    public static String validateFileName(MultipartFile file, String fileName) {
        if (!StringUtils.hasText(fileName)) {
            fileName = file.getOriginalFilename();
        }

        String regex = "[\\p{Cntrl}/\\\\\0<>:\"|]";

        if (!StringUtils.hasText(fileName) || fileName.matches(regex) || fileName.length() > 255) {
            String errorMessage = "Invalid filename: ";
            if (!StringUtils.hasText(fileName)) {
                errorMessage += "Please provide a filename.";
            } else if (fileName.matches(regex)) {
                errorMessage += "Avoid control characters, slashes (/ \\), null byte, or special characters like < > : \" |.";
            } else {
                errorMessage += "Filename is too long. Maximum length is 255 characters.";
            }
            throw new UploadFailedException(errorMessage, HttpStatus.BAD_REQUEST);
        }
        return fileName;
    }

    private static String calculateSha256Hash(MultipartFile file) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(file.getBytes());

        // Convert the byte array to a hexadecimal string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static List<Pair<Long, Long>> validateAndParseByteRange(String range, long fileSize) {
        ArrayList<Pair<Long, Long>> ranges = new ArrayList<>();
        if (range == null || range.isEmpty()) {
            return ranges;
        }
        if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
            throw new InvalidByteRangeException("Invalid byte range format", fileSize);
        }

        for (String part : range.substring(6).split(",")) {
            String[] split = part.split("-");
            long start = split[0].isEmpty() ? -1 : Long.parseLong(split[0]);
            long end = split[1].isEmpty() ? -1 : Long.parseLong(split[1]);

            if (start == -1) {
                start = fileSize - end;
                end = fileSize - 1;
            } else if (end == -1 || end > fileSize - 1) {
                end = fileSize - 1;
            }

            if (start > end) {
                throw new InvalidByteRangeException("Invalid byte range " + start + "-" + end, fileSize);
            }

            ranges.add(Pair.of(start, end));
        }

        return ranges;
    }

}
