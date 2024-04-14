package com.tf.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity(name = "file_metadata")
//@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FileMetadataModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID fileId;

    @Column(unique = true, nullable = false)
    private String fileName;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    private long size;

    private String type;

    //TODO remove this field
    private String path;

    private boolean isStoredInChunks;

    @Nonnull
    private String fileHash;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    public FileMetadataModel(@Nonnull String fileName, @Nonnull String fileHash, long size, String type) {
        this.fileName = fileName;
        this.fileHash = fileHash;
        this.size = size;
        this.type = type;
    }

    public static final class Builder {

        private String fileName;
        private String fileHash;
        private long size;
        private String type;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder fileName(@Nonnull String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder fileHash(@Nonnull String fileHash) {
            this.fileHash = fileHash;
            return this;
        }

        public Builder size(long size) {
            this.size = size;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public FileMetadataModel build() {
            return new FileMetadataModel(fileName, fileHash, size, type);
        }
    }
}
