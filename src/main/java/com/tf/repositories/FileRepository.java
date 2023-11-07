package com.tf.repositories;

import com.tf.models.FileModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileRepository extends JpaRepository<FileModel, UUID> {
}
