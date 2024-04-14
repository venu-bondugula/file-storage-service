package com.tf.repositories;

import com.tf.models.FileChunkMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface FileChunkMetadataRepository extends JpaRepository<FileChunkMetadata, UUID> {
    @Query("SELECT fcm FROM file_chunk_metadata fcm WHERE fcm.fileMetadata.fileId = ?1 ORDER BY fcm.sequenceNumber ASC")
    List<FileChunkMetadata> findAllByFileIDOrderBySequenceNumberAsc(UUID fileMetadataId);

}
