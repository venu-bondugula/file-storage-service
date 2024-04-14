# FileController API Documentation

The `FileController` class provides the following endpoints:

## Endpoints

### Upload File

#### Request

- Method: `POST`
- Path: `/api/files/upload`
- Request Body:
  - `file`: File to upload
  - `name`: Name of the file (optional)
  - 'fileHash': Hash of the file (optional)
- Content-Type: `multipart/form-data`
- Description: Uploads a file to the server.
- Example:

```json
{
  "file": "file",
  "name": "file1",
  "fileHash": "1234567890"
}
```

#### Response

- Status: `200 OK`
- Description: File uploaded successfully.
- Example:

```json
{
  "id": 1,
  "name": "file1",
  "size": 1000,
  "createdAt": "2021-10-10 10:10:10",
  "updatedAt": "2021-10-10 10:10:10",
  "path": "/path/to/file1",
  "userId": 1,
  "type": "txt",
  "fileHash": "1234567890",
  "isStoredInChunks": false
}
```

### Download File

#### Request

- Method: `GET`
- Path: `/api/files/download/{id}`
- Path Variables:
  - `id`: ID of the file to download
  - Description: Downloads a file from the server.
  - Response Content-Type: `application/octet-stream`
  - Example:
  - Request: `/api/files/download/1`

#### Response

- Status: `200 OK`
- Description: File downloaded successfully.
- Response: File content

### Update File

#### Request

- Method: `PUT`
- Path: `/api/files/update/{id}`
- Path Variables:
  - `id`: ID of the file to update
  - Request Body:
    - `name`: New name of the file (optional)
    - `fileHash`: New hash of the file (optional)
    - `file`: New file to upload (optional)
    - Content-Type: `multipart/form-data`
    - Description: Updates a file on the server.
    - Example:
    - Request: `/api/files/update/1`
    - Request Body:
    - `name`: "file1_updated"
      - `fileHash`: "0987654321"
      - `file`: "file_updated"
      - Response
    - Status: `200 OK`
    - Description: File updated successfully.
    - Example:
    - Response:
    - `id`: 1
      - `name`: "file1_updated"
      - `size`: 2000
      - `createdAt`: "2021-10-10 10:10:10"
      - `updatedAt`: "2021-10-10 10:10:10"
      - `path`: "/path/to/file1_updated"
      - `userId`: 1
      - `type`: "txt"
      - `fileHash`: "0987654321"
      - `isStoredInChunks`: false


### Delete File

#### Request

- Method: `DELETE`
- Path: `/api/files/delete/{id}`
- Path Variables:
  - `id`: ID of the file to delete
  - Description: Deletes a file from the server.
  - Example:
  - Request: `/api/files/delete/1`

#### Response

- Status: `200 OK`
- Description: File deleted successfully.


### Get List of Files

#### Request

- Method: `GET`
- Path: `/api/files/list`
- Description: Retrieves a list of files from the server.
- Example:
- Request: `/api/files/list`
- Response
- Status: `200 OK`
- Description: List of files retrieved successfully.
- Example:
- Response:

```json
[
  {
    "id": 1,
    "name": "file1",
    "size": 1000,
    "createdAt": "2021-10-10 10:10:10",
    "updatedAt": "2021-10-10 10:10:10",
    "path": "/path/to/file1",
    "userId": 1,
    "type": "txt",
    "fileHash": "1234567890",
    "isStoredInChunks": false
  },
  {
    "id": 2,
    "name": "file2",
    "size": 2000,
    "createdAt": "2021-10-10 10:10:10",
    "updatedAt": "2021-10-10 10:10:10",
    "path": "/path/to/file2",
    "userId": 1,
    "type": "txt",
    "fileHash": "1234567890",
    "isStoredInChunks": false
  }
]
```