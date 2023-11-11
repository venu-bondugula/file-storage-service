package com.tf.services;

import com.tf.exception.UploadFailedException;
import com.tf.helpers.InputValidator;
import com.tf.models.FileModel;
import com.tf.repositories.FileRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
public class FileService {
    private FileRepository fileRepository;

    public Map<String, String> getStringStringMap() {
        Map<String, String> response = new HashMap<>();
        response.put("hello", "world");
        return response;
    }

    public ResponseEntity<FileModel> uploadFile(MultipartFile file, String fileHash, String fileName, String createdBy) {
        InputValidator.validate(file, fileHash);
        try {
            FileModel payload = new FileModel(fileName);
            if (createdBy != null && !createdBy.isBlank())
                payload.setCreated_by(createdBy);
            FileModel savedFile = fileRepository.save(payload); // Save the file and get a unique identifier
            return new ResponseEntity<>(savedFile, HttpStatus.CREATED);
        } catch (Exception e) {
            throw new UploadFailedException();
        }
    }

    public ResponseEntity<FileModel> downloadFile(String fileId) {
        UUID uuid = UUID.fromString(fileId);
        FileModel savedFile = fileRepository.getReferenceById(uuid);
        return new ResponseEntity<>(savedFile, HttpStatus.CREATED);
    }

}
