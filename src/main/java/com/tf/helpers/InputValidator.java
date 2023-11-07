package com.tf.helpers;

import com.tf.exception.EmptyFileException;
import com.tf.exception.FileHashMismatchException;
import com.tf.exception.FileNotProvidedException;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class InputValidator {
    public static void validate(MultipartFile file, String fileHash) {
        if (file == null) {
            throw new FileNotProvidedException();
        }
        if (file.isEmpty()) {
            throw new EmptyFileException();
        }

        if (!validateFileHash(file, fileHash)) {
            throw new FileHashMismatchException();
        }
    }

    private static boolean validateFileHash(MultipartFile file, String fileHash) {
        String uploadedFileHash = calculateFileHash(file);
        System.out.println("file hash is : " + uploadedFileHash);
        return uploadedFileHash.equalsIgnoreCase(fileHash);

    }

    private static String calculateFileHash(MultipartFile file) {
        // Calculate the hash using DigestUtils or another hash algorithm
        try {
            return DigestUtils.md5DigestAsHex(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String calculateSha256Hash(MultipartFile file) throws IOException, NoSuchAlgorithmException {
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
}
