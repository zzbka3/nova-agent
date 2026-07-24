package com.nova.agent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUser {
    private Long accountId;
    private String accountName;
    private String token;
}
