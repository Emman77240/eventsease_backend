package com.eventease.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String startDateTime;
    private String endDateTime;
    private String location;
    private String status;
    private Boolean isPublic;
    private String imageUrl;
    private String eventDate;
    // add other fields as needed
}

