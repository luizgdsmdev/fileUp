package com.bytebybyte.fileup.Application.Mappings.Upload;

import com.bytebybyte.fileup.Application.DTOs.Response.Upload.UploadSessionResponse;
import com.bytebybyte.fileup.Domain.Entities.Upload.UploadSessionEntity;
import com.bytebybyte.fileup.Domain.Entities.User.UserEntity;
import com.bytebybyte.fileup.Domain.Enums.Upload.UploadStatus;
import org.springframework.stereotype.Component;

@Component
public class UploadMapping {

    /**
     * Method to map a new UploadSessionEntity DTO to the UploadSessionEntity entity.
     * The attributes not mentioned below are initialized with default values.
     * @param userEntityId UserEntity ID -> UUID type
     * @param sessionName Session name type String
     * @param fileSizeBytes File size type long
     * @param chunkSizeBytes Chunk size type long
     * @param totalChunks Total chunks type int
     * @return UploadSessionEntity entity
     */
    public UploadSessionEntity toNewUploadSessionEntity(
            UserEntity userEntityId,
            String sessionName,
            long fileSizeBytes,
            long chunkSizeBytes,
            int totalChunks
    ) {
        return new UploadSessionEntity(
                null,
                userEntityId,
                sessionName,
                fileSizeBytes,
                chunkSizeBytes,
                totalChunks,
                0,
                UploadStatus.CREATED,
                false,
                null,
                null
        );
    }


    /**
     * Method to map the UploadSessionEntity entity to the UploadSessionEntity DTO.
     * @param uploadSessionEntity UploadSessionEntity entity
     * @return UploadSessionResponse DTO
     */
    public UploadSessionResponse toUploadSessionResponse(UploadSessionEntity uploadSessionEntity){
        return new UploadSessionResponse(
                uploadSessionEntity.getSessionId(),
                uploadSessionEntity.getUserEntity().getId(),
                uploadSessionEntity.getUserEntity().getFirstName(),
                uploadSessionEntity.getSessionName(),
                uploadSessionEntity.getFileSizeBytes(),
                uploadSessionEntity.getChunkSizeBytes(),
                uploadSessionEntity.getTotalChunks(),
                uploadSessionEntity.getUploadedChunks(),
                uploadSessionEntity.getStatus(),
                uploadSessionEntity.isCompleted(),
                uploadSessionEntity.getCreatedAt(),
                uploadSessionEntity.getUpdatedAt()
        );
    }
}
