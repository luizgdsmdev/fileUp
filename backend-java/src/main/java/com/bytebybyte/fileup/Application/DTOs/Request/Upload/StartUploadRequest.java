package com.bytebybyte.fileup.Application.DTOs.Request.Upload;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StartUploadRequest(

        @NotBlank(message = "File name is required")
        @Size(max = 100, message = "Email must have at most 100 characters")
        String fileName,

        @NotNull(message = "File size is required")
        long fileSize
) {}
