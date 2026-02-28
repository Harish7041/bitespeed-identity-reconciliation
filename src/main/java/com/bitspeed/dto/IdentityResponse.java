package com.bitspeed.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class IdentityResponse {
    private ContactDto contact;

    @Data
    @Builder
    public static class ContactDto {
        // Required misspelling from Bitespeed PDF
        private Integer primaryContatctId;
        private List<String> emails;
        private List<String> phoneNumbers;
        private List<Integer> secondaryContactIds; // Ensure this is Integer
    }
}