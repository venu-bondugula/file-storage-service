package com.tf.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChunkMetadata {
    private int chunkNumber;
    private long size;
    // Add other metadata properties as needed
}
