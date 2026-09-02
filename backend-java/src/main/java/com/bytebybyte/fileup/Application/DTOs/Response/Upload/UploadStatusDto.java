package com.bytebybyte.fileup.Application.DTOs.Response.Upload;


import com.bytebybyte.fileup.Domain.Enums.Upload.UploadStatus;

import java.util.List;
import java.util.Set;

public record UploadStatusDto(
        Set<Integer> uploadSucceeded,
        List<Integer> uploadMissing,
        UploadStatus status
) {}
