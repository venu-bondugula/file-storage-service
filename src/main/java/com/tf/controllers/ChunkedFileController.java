package com.tf.controllers;

import com.tf.dto.ChunkMetadata;
import com.tf.services.ChunkedFileService;

import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/chunks")
@AllArgsConstructor
public class ChunkedFileController {
    private ChunkedFileService chunkedFileService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFileChunk(@RequestParam("fileId") String fileId, @RequestParam("chunkNumber") int chunkNumber, @RequestParam("metadata") String metadata, @RequestPart("chunk") MultipartFile chunk) {
        // Parse and process metadata as needed

        // Create a ChunkMetadata object from metadata

        // Store the chunk and its metadata
        ChunkMetadata chunkMetadata = new ChunkMetadata(chunkNumber, chunk.getSize());
        chunkedFileService.uploadChunk(fileId, chunkNumber, chunk, chunkMetadata);

        // Return a success response
        return new ResponseEntity<>("Chunk uploaded successfully", HttpStatus.OK);
    }
}
