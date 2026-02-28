package com.bitspeed.dto;
import lombok.Data;

@Data
public class IdentityRequest {
    private String email;
    private String phoneNumber;
}