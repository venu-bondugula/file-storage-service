package com.tf.models;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity(name = "user_access")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserAccess {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID user_access_id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "user_id")
    @Nonnull
    private User user;

    @ManyToOne
    @JoinColumn(name = "file_id")
    @Nonnull
    private FileMetadataModel fileMetadataModel;

    @Nonnull
    private Privilege privilege;

    public enum Privilege {
        READER,
        EDITOR,
        OWNER
    }
}
