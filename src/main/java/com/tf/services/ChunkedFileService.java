package com.tf.services;

import com.tf.dto.ChunkMetadata;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChunkedFileService {

    private final Map<String, List<ChunkMetadata>> fileChunks = new HashMap<>();

    public void uploadChunk(String fileId, int chunkNumber, MultipartFile chunk, ChunkMetadata chunkMetadata) {
        // Implement logic to store the file chunk and its metadata
        // You can store the chunks in memory or a persistent storage system
        // Here, we'll store them in memory using a Map

        // If the fileId doesn't exist in the map, create an entry for it
        if (!fileChunks.containsKey(fileId)) {
            fileChunks.put(fileId, new ArrayList<>());
        }

        List<ChunkMetadata> chunks = fileChunks.get(fileId);

        // Store the chunk metadata (e.g., chunk number, size, etc.)
        chunkMetadata.setChunkNumber(chunkNumber);
        chunks.add(chunkMetadata);

        // You can save the actual chunk data to a file or another storage system
    }

    public List<ChunkMetadata> getFileChunkMetadata(String fileId) {
        // Retrieve and return the list of chunk metadata for a file
        return fileChunks.get(fileId);
    }
}
