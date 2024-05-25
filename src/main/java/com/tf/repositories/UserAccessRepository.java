package com.tf.repositories;

import com.tf.models.FileMetadataModel;
import com.tf.models.User;
import com.tf.models.UserAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserAccessRepository extends JpaRepository<UserAccess, UUID> {
    //    @Query(value = "SELECT * FROM user_access WHERE file_id LIKE ?1 AND user_id LIKE ?2 order by " +
//            "user_access_id limit 1", nativeQuery = true)
    @Query("SELECT ua FROM user_access ua WHERE ua.fileMetadataModel.file_id = ?1 AND ua.user.user_id = ?2 AND ua.privilege > ?3")
    UserAccess getUserAccessForTheFileToTheUser(UUID fileId, Long userId, UserAccess.Privilege privilege);

    @Query(value = "SELECT f.fileName FROM user_access ua JOIN ua.fileMetadataModel f WHERE f.fileName = ?1 AND ua.user = ?2")
    List<String> findAllFilesWithExactNameAccessibleByUser(String baseName, User user);

    @Query(value = "SELECT f.fileName FROM user_access ua JOIN ua.fileMetadataModel f WHERE f.fileName LIKE ?1% AND ua.user = ?2")
    List<String> findAllFilesWithSimilarNameAccessibleByUser(String baseName, User user);

    @Query(value = "SELECT f FROM user_access ua JOIN ua.fileMetadataModel f WHERE ua.user = ?1")
    List<FileMetadataModel> findAllFilesByUser(User user);

    //    @Modifying
    @Query("DELETE FROM user_access ua WHERE ua.fileMetadataModel.file_id = ?1")
    void deleteAllByFileId(UUID fileId);
}
