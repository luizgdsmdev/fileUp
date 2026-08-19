package com.bytebybyte.fileup.Application.DTOs.Response.Upload;

import com.bytebybyte.fileup.Domain.Enums.Upload.UploadStatus;

import java.time.Instant;
import java.util.UUID;

public record UploadSessionResponse(
        UUID sessionId,
        UUID userId,
        String firstName,
        String sessionName,
        long fileSizeBytes,
        long chunkSizeBytes,
        int totalChunks,
        int uploadedChunks,
        UploadStatus status,
        boolean completed,
        Instant createdAt,
        Instant updatedAt
) {}
