# Drop Box Equivalent Storage Service (Backend)
## 1. Overview
This project aims to develop a backend service that replicates the functionalities of Dropbox, a cloud storage platform. Currently, this implementation only focuses on the backend aspects of such a service, providing features like user authentication, file upload/download/update/deletion, and a comprehensive database design for managing user and file metadata.
### Key features:
- User Authentication and Authorization
- File Upload/Download/Update/Delete operations
- Efficient File Chunking for Large Files
- Comprehensive Database Design
- Dockerized Deployment for Easy Setup
- Custom Exception Handling and Rate Limiting
- Integration with Swagger UI for API Testing

## 2. Installation Steps
### Requirements:
- Java 11 or higher
- Maven
- Docker

### Instructions:

1. **Fork this Repository:** Clone this repository onto your local machine using the git clone command.
2. **Create a Codespace:** Go to the GitHub Codespaces website and create a new codespace for your forked repository. This may take a few minutes to initialize.
3. **Compile and Build:** Once your Codespace is ready, open a terminal window and run the following commands:
- `mvn clean install`: This compiles and builds the project.
- `docker compose up`: This brings up both the MySQL database and the application server in separate Docker containers.
  Please be patient as it may take several minutes for everything to start up.
4. **Access the APIs:** Once the setup is complete, the API will be available on port 8080.
5. **Swagger UI:** Navigate to /swagger-ui.html after the port 8080 address to access a user interface for testing and
   exploring the APIs.


## 3. High-Level Design
The application utilizes a Spring Boot framework with RESTful API endpoints for file upload/download operations and user authentication.

### Architecture:
- API Gateway (not included in this implementation): Routes incoming API requests to the appropriate backend service
- Backend Service (implemented): Handles user authentication, file management, storage operations, and database
  interactions

### Components:

- **AuthController:** Manages user registration and login functionalities using JWT tokens for authentication
- **FileController:** Facilitates upload, download, update, and deletion operations for files stored in the system
- **Database Layer:** Manages the storage and retrieval of user and file metadata using MySQL database

**File Chunking (for Large Files):** Large files are split into smaller chunks for efficient storage, transmission, and
access. This mechanism allows efficient handling of files that might exceed available memory or bandwidth limitations.

## 4. APIs Overview
This section provides a detailed description of the available API endpoints.

### Authentication

#### Signup
##### api/v1/auth/signup [POST]
This POST request registers a new user by accepting username and password in the JSON request body.

#### login
##### api/v1/auth/login [POST]
This POST request authenticates an existing user using username and password credentials in the JSON request body and returns a JWT token upon successful login.

### File Operations

#### upload
##### api/v1/files [POST]
This POST request uploads a file by accepting the file itself in multipart/form-data along with metadata like file
name, hash. If new file has same
name as existing file, this file will be stored with the same name appended with a number.

#### download
##### api/v1/files/{id} [GET]
This GET request retrieves a specific file using the file ID and provides access through a download link.
- This endpoint also supports partial downloads via the Range header for improved efficiency in large file scenarios.

#### update
##### api/v1/files/{id} [PUT]
This PUT request allows updating the content of an existing file using the provided file ID.

#### delete
##### api/v1/files/{id} [DELETE]
This PUT request deletes a specific file using the provided file ID and removes associated file information from the database.

#### fetch_all_files
##### api/v1/files [GET]
This PUT request fetches metadata information of all files accessible to a particular user.

## 5. Database Design
The project utilizes three main entities in its database schema:

### User Entity:
- **Table:** User
- **Fields:** id (unique identifier), username, password, roles

### File Metadata Entity:
- **Table:** FileMetadataModel
- **Fields:** id (unique identifier), name, size, createdAt, updatedAt, path, userId, isStoredInChunks

### File Chunk Entity:
- **Table:** FileChunkMetadata
- **Fields:** id (unique identifier), sequenceNumber, fileId, path, size

## 6. Additional Functionalities
- **Security:**
   - User access is restricted to their own files, ensuring data security and privacy.
- **Error Handling:**
   - Custom exception classes and handlers provide informative error messages with relevant details and status codes for easier debugging and handling.
- **Performance:**
   - Efficient chunked data processing optimizes performance for both small and large file handling.
   - Dockerization enables fast environment setup, portability, and scalability.
- **Testing:**
   - Integration with Swagger UI provides a convenient interface for testing APIs.
- **Rate Limiting:**
   - Rate limiting is implemented to prevent abuse and ensure fair usage of the service.


## 7. Future Enhancements
The current implementation focuses primarily on core backend functionalities and data storage. Potential improvements and additions could involve:

- Frontend Development: Implementing a user-friendly interface for interacting with the API and managing file uploads, downloads, etc.
- Additional Storage Options: Exploring integration with external cloud storage solutions for increased capacity and scalability.
- File Versioning: Enabling the storage of multiple versions of a file for better history tracking and recovery options.
- Advanced Search Features: Adding functionality to search files based on keywords, metadata fields, etc., for improved organization and accessibility.
- User Management Enhancements: Implementing features for role-based access control, password resets, and other user-centric functionalities.

## Conclusion
This backend project serves as a solid foundation for building a robust cloud-based file storage service with functionalities similar to Drop Box. It offers comprehensive database design, secure user access controls, efficient chunked storage, and well-documented APIs, paving the way for further enhancements and customized implementations to cater to specific needs and preferences