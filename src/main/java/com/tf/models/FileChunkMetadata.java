package com.tf.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity(name = "file_chunk_metadata")
@Data
public class FileChunkMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "fileId", nullable = false)
    private FileMetadataModel fileMetadata;

    @Column(name = "sequence_number", nullable = false)
    private int sequenceNumber;

    @Column(name = "path", nullable = false)
    private String path;

    @Column(name = "size")
    private Long size;

    public FileChunkMetadata(FileMetadataModel payload, int sequenceNumber, String path, long size) {
        this.fileMetadata = payload;
        this.sequenceNumber = sequenceNumber;
        this.path = path;
        this.size = size;
    }

    public FileChunkMetadata() {

    }
}
