package com.tf.models;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;

import java.time.Instant;
import java.util.UUID;

@Entity
//@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class FileModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    private String fileName;

    @CreationTimestamp
    private Instant created_at;

    @UpdateTimestamp
    private Instant updated_at;

    @CreatedBy
    private String created_by = "default";
}
