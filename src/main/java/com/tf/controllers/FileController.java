package com.tf.controllers;

import com.tf.models.FileModel;
import com.tf.services.FileService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("api/v1/file")
@AllArgsConstructor
public class FileController {

    private FileService fileService;

    /**
     * @return simple "Hello World" string
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> helloWorld() {
        return fileService.getStringStringMap();
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces =
            MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<FileModel> uploadFile(@RequestPart MultipartFile file, @RequestPart String fileName, @RequestPart String createdBy, @RequestPart String fileHash
    ) {
        return fileService.uploadFile(file, fileHash, fileName, createdBy);
    }

    @GetMapping(path = "/download/{file_id}")
    public @ResponseBody ResponseEntity<FileModel> downloadFile(@PathVariable String file_id){
        return fileService.downloadFile(file_id);
    }
}
