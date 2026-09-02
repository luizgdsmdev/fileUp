package com.bytebybyte.fileup.Controllers.Upload;


import com.bytebybyte.fileup.Application.DTOs.Request.Upload.StartUploadRequest;
import com.bytebybyte.fileup.Application.DTOs.Response.Upload.UploadSessionResponse;
import com.bytebybyte.fileup.Application.DTOs.Response.Upload.UploadStatusDto;
import com.bytebybyte.fileup.Application.Services.Upload.UploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/upload")
public class UploadController {

    private final UploadService _uploadService;

    /**
     * Starting point for the process of uploading a file.
     * @param uploadRequest DTO from request
     * @return ResponseEntity<UploadSessionResponse>
     */
    @PostMapping
    public ResponseEntity<UploadSessionResponse> uploadFile(@Valid @RequestBody StartUploadRequest uploadRequest){
        return _uploadService.startUploadSession(uploadRequest);
    }


    /**
     * Uploading each chunk of the file from the session
     * @param sessionId previously generated session ID at uploadFile endpoint
     * @param chunkHash hash of the chunk, calculated at the client side
     * @param chunkIndex index of the chunk, based on the total number of chunks from the session
     * @param chunkBody chunk body, the actual file data
     * @return ResponseEntity<?>
     */
    @PutMapping
    public ResponseEntity<?> uploadChunk(
            @RequestHeader UUID sessionId,
            @RequestHeader String chunkHash,
            @RequestHeader int chunkIndex,
            @RequestBody byte[] chunkBody
            ){

        return _uploadService.uploadChunk(sessionId, chunkHash, chunkIndex, chunkBody);
    }

    /**
     * Get the status of the upload session
     * @param uploadId session identifier
     * @return ResponseEntity<UploadStatusDto>
     */
    @GetMapping
    public ResponseEntity<UploadStatusDto> getUploadStatus(@RequestHeader UUID uploadId){

        return _uploadService.getUploadStatus(uploadId);
    }

}
