package com.bytebybyte.fileup.Infrastructure.Persistence.Interfaces.Uploads;

import com.bytebybyte.fileup.Domain.Entities.Upload.UploadSessionEntity;
import com.bytebybyte.fileup.Domain.Enums.Upload.UploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IUploadRepository extends JpaRepository<UploadSessionEntity, UUID> {

    List<UploadSessionEntity> findByUserEntity_Id(UUID userId);

    List<UploadSessionEntity> findByUserEntity_IdAndStatus(
            UUID userId,
            UploadStatus status
    );

    Optional<UploadSessionEntity> findBySessionIdAndUserEntity_Id(
            UUID sessionId,
            UUID userId
    );

    Optional<UploadSessionEntity> findBySessionIdAndUserEntity_IdAndStatus(
            UUID sessionId,
            UUID userId,
            UploadStatus status
    );

}
