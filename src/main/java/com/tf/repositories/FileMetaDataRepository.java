package com.tf.repositories;

import com.tf.models.FileMetadataModel;
import com.tf.models.User;
import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileMetaDataRepository extends JpaRepository<FileMetadataModel, UUID> {
    @Nonnull
    FileMetadataModel getReferenceById(@Nonnull UUID fileUUID);

    @Nonnull
    @Override
    Optional<FileMetadataModel> findById(@Nonnull UUID uuid);

    boolean existsByFileNameAndUser(String fileName, User user);

    @Query("SELECT fileName FROM file_metadata WHERE fileName LIKE ?1% AND user LIKE ?2")
    List<String> findFileNamesStartingWithFileName(String fileName, User user);

    List<FileMetadataModel> findAllByUserId(Long userId);

}
