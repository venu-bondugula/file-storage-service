# File Storage API Documentation
This document provides an overview of the file storage API along with instructions to run the application locally and test the functionalities.

# Running the application
1. Fork this repo, 
2. Create a github codespace for this repository on master branch, it may take few mins for the first time.
3. Upon, navigating to VS code editor, open the TERMINAL tab in the bottom panel.
    1. Compile the app by running `mvn clean install` (This may take couple of mins).
    2. Bring both mysql and app live with `docker compose up` (This may take couple of mins, tries to bring up mysql post that boots up app wait till you see log with message "Picked storage.path as ./data").
4. Navigate to PORTS section in the bottom panel where you can find the local address for the server running on port 
       8080, do a ctrl or cmd click on it, post opening of the link in new browser window navigate to swagger-ui by appending `/swagger-ui.html` to the url
5. Enjoy testing the api's from the swagger-ui.
---
# Testing the application

1. User Management:
   - Use the "Sign Up" operation to create a new user with a username and password (remember these credentials).
   - Perform a "Login" operation using the created username and password to obtain a JWT token for authentication.
2. API Interaction:
   - Locate the "Authorize" button on the top right corner of the Swagger UI, next to the server URL.
   - Click on "Authorize" and paste the obtained JWT token from the login step. Click "Close" after authorization.
3. File Upload:
   - Navigate to the "Upload a file" operation.
   - Select a file to upload and provide an optional new name in the "name" field.
   - Click "Execute" to upload the file. Upon successful upload, you'll receive a FileId.
4. List Files:
   - Go to the "Get all files" section.
   - Click the "Execute" button to retrieve a list of all uploaded files.
5. Download File:
   - Navigate to the "Download file" operation.
   - Enter the FileId of a previously uploaded file.
   - Click "Execute" to download the selected file.
6. Update File:
   - Go to the "Update file" operation.
   - Select a new file to upload.
   - Optionally, provide the old file name in the "name" field to retain it. Otherwise, the API will use the uploaded 
     file's name.
   - Click "Execute" to update the file.
7. Delete File:
   - Navigate to the "Delete file" operation.
   - Enter the FileId of the file you want to delete.
   - Click "Execute" to delete the file.

---
# API Documentation
   * [FileController](apiDocumentation/fileController.md)
   * [AuthController](apiDocumentation/authController.md)
---
# Database Design

## [User.java](src%2Fmain%2Fjava%2Fcom%2Ftf%2Fmodels%2FUser.java)

| Column Name | Data Type | Description                                      |
|-------------| --- |--------------------------------------------------|
| id          | long | Unique identifier for each user (auto-generated) |
| username    | varchar | Username of the user                             |
| password    | varchar | Password hash of the user                        |
|roles        | varchar | Roles of the user                                |

Here's the representation of the table design for the `UserModel` class:

| id | username | password      | roles |
|----|----------|---------------|-------|
| 1  | user1    | password1Hash | USER  |
| 2  | user2    | password2Hash | USER  |

## [FileMetadataModel.java](src%2Fmain%2Fjava%2Fcom%2Ftf%2Fmodels%2FFileMetadataModel.java)

| Column Name | Data Type | Description |
|-------------| --- | --- |
| id          | long | Unique identifier for each file (auto-generated) |
| name        | varchar | Name of the file |
| size        | long | Size of the file in bytes |
| createdAt   | timestamp | Time of upload of the file |
| updatedAt   | timestamp | Time of last modification of the file |
| path        | varchar | Path of the file in the server |
| userId      | long | Foreign key referencing the associated user |
|type         | varchar | Type of the file |
|fileHash     | varchar | Hash of the file |
|isStoredInChunks | boolean | Whether the file is stored in chunks or not |

Here's the representation of the table design for the `FileMetadataModel` class:

| id | name | size | createdAt | updatedAt | path | userId | type | fileHash | isStoredInChunks |
|----|------|------|-----------|-----------|------|--------|------|----------|------------------|
| 1  | file1 | 1000 | 2021-10-10 10:10:10 | 2021-10-10 10:10:10 | /path/to/file1 | 1 | txt | 1234567890 | false |
| 2  | file2 | 2000 | 2021-10-10 10:10:10 | 2021-10-10 10:10:10 | /path/to/file2 | 1 | txt | 1234567890 | false |
| 3  | file3 | 3000 | 2021-10-10 10:10:10 | 2021-10-10 10:10:10 | /path/to/file3 | 1 | txt | 1234567890 | false |

## [FileChunkMetadata.java](src%2Fmain%2Fjava%2Fcom%2Ftf%2Fmodels%2FFileChunkMetadata.java)

| Column Name | Data Type | Description |
|-------------| --- | --- |
| id          | long | Unique identifier for each file chunk (auto-generated) |
|sequence_number | int | Sequence number of the chunk |
|fileId       | long | Foreign key referencing the associated file |
|path         | varchar | Path of the file chunk in the server |
|size         | long | Size of the file chunk in bytes |

Here's the representation of the table design for the `FileChunkMetadata` class:

| id | sequence_number | fileId | path | size |
|----|-----------------|--------|------|------|
| 1  | 1               | 1      | /path/to/file1_chunk1 | 1000 |
| 2  | 2               | 1      | /path/to/file1_chunk2 | 1000 |
| 3  | 1               | 2      | /path/to/file2_chunk1 | 1000 |

---

# General things considered during development
## Functional
1. **Security**: JWT based authentication is used to secure the API's. (This ensures only authorized users can access the API)
2. **File User Association**: File is associated with the user who uploaded it. (This allows tracking ownership of uploaded files)
3. **Error Handling**: Custom exception handling is done to provide meaningful error messages. (This improves user experience by providing clear error details)
5. **API Documentation**: Swagger is used for API documentation. (This allows developers to easily understand and integrate with the API)
6. **Database**: MySQL is used as the database. (This specifies the data storage technology)
7. **File Upload**: File upload is done using MultipartFile based on the value passed in application.properties file. (This defines the method for uploading files)
8. **File Storage**: Files are stored in the local file system. (This specifies the location where uploaded files are kept)
10. **File Update**: File update is done by uploading a new file and deleting the old file. (This defines the logic for updating files)
11. **File Delete**: File delete is done by deleting the file from the file system and file metadata from the database. (This defines the logic for deleting files)
19. **Byte Range Request**: Single/Multi Byte range request is supported for file download. (This allows for efficient downloading of large files in parts)

## Non-Functional
12. **File Chunking**: File chunking is done to store large files. (This improves performance for large file uploads)
13. **File Metadata**: File metadata is stored in the database. (This defines additional information stored about the file)
14. **File Chunk Metadata**: File chunk metadata is stored in the database. (This is specific to the chunking implementation)
15. **File Hashing**: File hashing is done to verify the integrity of the file. (This ensures data hasn't been corrupted during upload/download)
16. **Logging execution time**: Execution time of the API's is logged. (This helps monitor API performance)
17. **Rate Limiting**: Rate limiting is done using Guava RateLimiter. (This prevents abuse by limiting API requests per user/client)
18. **Request Id**: Request Id is generated for each request and passed in the response header. (This helps with troubleshooting and tracking requests)
20. **One command to run**: `docker compose up` to bring up the application and database. (This simplifies deployment)
21. **Easy to test**: Swagger UI is used for API testing. (This improves development workflow)
22. **File Upload Size**: File upload size is limited to 10MB. (This defines a restriction on file size for upload)
23. **File Download Size**: File download size is limited to 10MB. (This defines a restriction on file size for download)
