package com.tf.controllers;

import com.tf.dto.ChunkMetadata;
import com.tf.services.ChunkedFileService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.List;

@RestController
@RequestMapping("/files")
@AllArgsConstructor
public class DownloadFileController {
    private final ChunkedFileService chunkedFileService;

    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable("fileId") String fileId) {
        try {
            // Retrieve the list of chunk metadata for the specified file
            List<ChunkMetadata> chunkMetadataList = chunkedFileService.getFileChunkMetadata(fileId);

            if (chunkMetadataList == null || chunkMetadataList.isEmpty()) {
                return new ResponseEntity<>("File not found".getBytes(), HttpStatus.NOT_FOUND);
            }

            // Create an in-memory stream to assemble the file
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            for (ChunkMetadata chunkMetadata : chunkMetadataList) {
                // Retrieve the chunk data (you may need to fetch it from storage)
                byte[] chunkData = chunkedFileService.getChunkData(fileId, chunkMetadata.getChunkNumber());

                // Write the chunk data to the output stream
                outputStream.write(chunkData);
            }

            // Convert the assembled file to a byte array
            byte[] fileBytes = outputStream.toByteArray();

            // Set response headers (content type, content disposition, etc.)
            // Replace "application/octet-stream" with the appropriate content type
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", "downloaded-file.ext");

            // Return the assembled file as a ResponseEntity
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .body(fileBytes);
        } catch (Exception e) {
            return new ResponseEntity<>("File download failed".getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}