package com.nova.agent.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FileUploadRequest {
    @NotBlank(message = "app_id is required")
    private String app_id;
    @NotBlank(message = "conversation_id is required")
    private String conversation_id;
    private String file_url;
    private String file_name;
}
