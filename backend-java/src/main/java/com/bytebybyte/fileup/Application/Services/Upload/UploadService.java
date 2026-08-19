package com.bytebybyte.fileup.Application.Services.Upload;

import com.bytebybyte.fileup.Application.DTOs.Request.Upload.StartUploadRequest;
import com.bytebybyte.fileup.Application.DTOs.Response.Upload.UploadSessionResponse;
import com.bytebybyte.fileup.Application.Mappings.Upload.UploadMapping;
import com.bytebybyte.fileup.Application.Services.UploadRedisService.UploadRedisService;
import com.bytebybyte.fileup.Application.Utils.Auth.SecurityContextHelper;
import com.bytebybyte.fileup.Application.Utils.Upload.UploadHashProcess;
import com.bytebybyte.fileup.Domain.Entities.Upload.UploadSessionEntity;
import com.bytebybyte.fileup.Domain.Entities.User.UserEntity;
import com.bytebybyte.fileup.Domain.Exceptions.ConflictException;
import com.bytebybyte.fileup.Domain.Exceptions.NotFoundException;
import com.bytebybyte.fileup.Infrastructure.Persistence.Interfaces.Uploads.IUploadRepository;
import com.bytebybyte.fileup.Infrastructure.Persistence.Interfaces.Users.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadService {

    private final SecurityContextHelper _securityContextHelper;
    private final UploadMapping _uploadMapping;
    private final IUploadRepository _uploadRepository;
    private final IUserRepository _userRepository;
    private final UploadRedisService _uploadRedisService;
    private final RedisTemplate<String, Object> _redisTemplate;

    private static final int MAX_CHUNK_SIZE_BYTES = 1024 * 1024 * 10; // 10MB per chunk


    /**
     * Method to start a new upload session. It creates a new UploadSessionEntity object,
     * saves it into the database, and returns an UploadSessionResponse containing session details.
     * @param uploadRequest StartUploadRequest DTO from the controller layer
     * @return ResponseEntity<UploadSessionResponse> containing the session details
     */
    public ResponseEntity<UploadSessionResponse> startUploadSession(StartUploadRequest uploadRequest) {
        //Necessary for mapping to upload session entity
        UserEntity currentUser = _getAuthenticatedUser();

        int totalChunks = _getTotalChunks(uploadRequest.fileSize());

        // First we save into database for tracking and also for UUID generation (session_ID)
        // Then we save into redis for chunking and management (on uploadChunk method)
        UploadSessionEntity sessionDraft = _toNewUploadSessionEntity(
                                            currentUser,
                                            uploadRequest.fileName(),
                                            uploadRequest.fileSize(),
                                            totalChunks);

        // Save into database
        UploadSessionEntity createdSession = _uploadRepository.save(sessionDraft);

        // Convert to UploadSessionResponse before sharing with Redis for security
        // (avoids exposing the whole userEntity data)
        UploadSessionResponse response = _uploadMapping.toUploadSessionResponse(createdSession);


        return ResponseEntity.ok(response);
    }


    /**
     * Method to upload a chunk of the file. It verifies the chunk hash, creates the uploads directory,
     * @param sessionId session identifier
     * @param hashChunk hash of the chunk, calculated at the client side
     * @param chunkIndex chunk index related to session
     * @param chunkBody chunk data
     * @return ResponseEntity<?>
     */
    public ResponseEntity<?> uploadChunk(UUID sessionId, String hashChunk, int chunkIndex, byte[] chunkBody) {
        // First, verify that the chunk is not already uploaded to Redis
        String chunkHashKey = _uploadRedisService.createChunkHashKey(sessionId, chunkIndex, hashChunk);
        _uploadRedisService.isChunkPresent(chunkHashKey, chunkIndex);


        // Calculate the hash of the chunk body for verification with the hashChunk sent by the client
        String calculatedHash = UploadHashProcess.hashToSha256(chunkBody);
        _isClientHashValid(calculatedHash, hashChunk);


        // Creation of the uploads directory if it doesn't exist, followed by
        // the sessionId and a file for the chunk (using the chunkIndex as the filename)
        _createUploadsDirectory(sessionId, chunkIndex, chunkBody);


        // Add the chunk index to the set of uploaded chunks in Redis
        _redisTemplate.opsForSet().add(chunkHashKey, chunkIndex);
        // Also upload the chunk hash to Redis
        String redisHashKey = String.format("chunkKey:%s:%s:%s", sessionId, chunkIndex, hashChunk);
        _redisTemplate.opsForSet().add(redisHashKey, hashChunk);


        return ResponseEntity.ok().build();
    }





    // Supportive methods init --------

    /**
     * Creates the uploads directory if it doesn't exist and writes the chunk body to the file.
     * @param sessionId session identifier
     * @param chunkIndex chunk index related to session
     * @param chunkBody chunk data
     */
    private void _createUploadsDirectory(UUID sessionId, int chunkIndex, byte[] chunkBody){
        Path chunkPath = Path.of("uploads", sessionId.toString(), String.valueOf(chunkIndex));
        try{
            Files.createDirectories(chunkPath.getParent());
            Files.write(chunkPath, chunkBody);

        } catch (Exception e) {
            throw new RuntimeException("Error while writing chunk at UploadService_uploadChunk_method: ", e);
        }
    }

    /**
     * Basic validation of the hash sent by the client.
     * @param calculatedHash The hash calculated from the chunk body
     * @param hashChunk The hash sent by the client
     */
    private void _isClientHashValid(String calculatedHash, String hashChunk){
        if (!calculatedHash.equals(hashChunk)){
            throw new ConflictException("Hash verification failed due to value mismatch", "UploadService_uploadChunk_method");
        }
    }

    /**
     * Retrieves the current user's ID from the security context.
     * Validates that the user exists in the database.
     * @return The current user's ID.
     */
    private UserEntity _getAuthenticatedUser(){
        //Necessary for mapping to upload session entity
        UUID userId = _securityContextHelper.getCurrentUserId();


        // Validate that the user exists in the database, if not, throw a NotFoundException
        return  _userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User not found",
                        "UploadService_startUploadSession_method"));
    }


    /**
     * Calculates the total number of chunks needed to upload the file.
     * @param fileSize The size of the file in bytes.
     * @return The total number of chunks needed to upload the file.
     */
    private int _getTotalChunks(long fileSize){
        return Math.toIntExact(Math.ceilDiv(
                fileSize,
                MAX_CHUNK_SIZE_BYTES
        ));
    }


    /**
     * Creates a new UploadSessionEntity object by mapping the given parameters.
     * @param currentUser userEntity from the security context (authenticated user)
     * @param fileName String type from the request
     * @param fileSize long type from the request
     * @param totalChunks int type from the request
     * @return UploadSessionEntity object
     */
    private UploadSessionEntity _toNewUploadSessionEntity(
            UserEntity currentUser,
            String fileName,
            long fileSize,
            int totalChunks
    ){

        // Necessary for mapping to upload session entity
        // I separated the mapping for better legibility on main method
        return _uploadMapping
                .toNewUploadSessionEntity(
                        currentUser,
                        fileName,
                        fileSize,
                        MAX_CHUNK_SIZE_BYTES,
                        totalChunks
                );
    }

}
