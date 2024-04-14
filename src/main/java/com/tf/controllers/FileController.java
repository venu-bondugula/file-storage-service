package com.tf.controllers;

import com.tf.models.FileMetadataModel;
import com.tf.services.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/v1/files")
@SecurityRequirement(name = "bearerAuth")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Upload a file",
            description = "Uploads a file to the server.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {@ApiResponse(responseCode = "200", description = "File uploaded successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FileMetadataModel.class)))})
    public @ResponseBody ResponseEntity<FileMetadataModel> uploadFile(@RequestPart MultipartFile file,
                                                                      @RequestPart(required = false) String fileName,
                                                                      @RequestPart(required = false) String fileHash,
                                                                      Authentication authentication
    ) {
        return fileService.uploadFile(file, fileName, fileHash, authentication);
    }

    @GetMapping(path = "/{file_id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(summary = "Download a file",
            parameters = {@Parameter(name = "file_id", description = "The ID of the file to download", required = true, in = ParameterIn.PATH),
                    @Parameter(name = "Range", description = "range of bytes to be fetched", in = ParameterIn.HEADER)},
            responses = {@ApiResponse(responseCode = "200", description = "File downloaded successfully", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))})
    public @ResponseBody ResponseEntity<ByteArrayResource> readFile(@PathVariable String file_id,
                                                                    @RequestHeader HttpHeaders headers,
                                                                    Authentication authentication) {
        return fileService.downloadFile(file_id, headers, authentication);
    }

    @PutMapping(path = "/{file_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update File",
            description = "Updates an existing file in the server.",
            parameters = {@Parameter(name = "file_id", description = "The ID of the file to update", required = true, in = ParameterIn.PATH)},
            responses = {@ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = FileMetadataModel.class), mediaType = MediaType.APPLICATION_JSON_VALUE))})
    public @ResponseBody ResponseEntity<FileMetadataModel> updateFile(@PathVariable String file_id,
                                                                      @RequestPart(required = false) MultipartFile file,
                                                                      @RequestPart(required = false) String fileName,
                                                                      @RequestPart(required = false) String fileHash,
                                                                      Authentication authentication
    ) {
        return fileService.updateFile(file_id, file, fileHash, fileName, authentication);
    }

    @DeleteMapping(path = "/{file_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete File",
            description = "Deletes a file from the server.",
            responses = {@ApiResponse(responseCode = "204", description = "File deleted successfully")})
    public @ResponseBody ResponseEntity<Object> deleteFile(@PathVariable String file_id,
                                                           Authentication authentication) {
        return fileService.deleteFile(file_id, authentication);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "List all files",
            responses = {@ApiResponse(responseCode = "200", description = "List of all files", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = List.class, type = "array")))})
    public ResponseEntity<List<FileMetadataModel>> listAllFiles(Authentication authentication) {
        return fileService.listAllFiles(authentication);
    }
}
