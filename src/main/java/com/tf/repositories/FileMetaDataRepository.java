package com.tf.repositories;

import com.tf.models.FileMetadataModel;
import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FileMetaDataRepository extends JpaRepository<FileMetadataModel, UUID> {
    @Nonnull
    FileMetadataModel getReferenceById(@Nonnull UUID fileUUID);

    @Nonnull
    @Override
    Optional<FileMetadataModel> findById(@Nonnull UUID uuid);
}
