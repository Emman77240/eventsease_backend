package com.eventease.controller;

import com.eventease.common.Constant;
import com.eventease.domain.entity.Event;
import com.eventease.dto.*;
import com.eventease.service.EventService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventResponse> createEvent(@ModelAttribute EventRequest request) {
        Event event = eventService.createEvent(request);
        return ResponseEntity.ok(EventResponse.builder()
                .data(null)
                .responseCode(Constant.SUCCESS)
                .responseMessage(Constant.EVENT_CREATED_SUCCESSFULLY)
                .build());

    }

    @GetMapping("/get-All")
    public ResponseEntity<EventResponse> getAllEvents() {
        List<EventResponseDTO> events = eventService.getAllEvents();
        return ResponseEntity.ok(EventResponse.builder()
                .data(events)
                .responseCode(Constant.SUCCESS)
                .responseMessage(Constant.EVENT_LIST_SUCCESSFULLY)
                .build());
    }

    @PostMapping("/get-by-id")
    public ResponseEntity<EventResponse> getEventById(@RequestBody GetEventRequest request) {
        EventResponseDTO event = eventService.getEventById((long) request.getId());
        return ResponseEntity.ok(EventResponse.builder()
                .data(event)
                .responseCode(Constant.SUCCESS)
                .responseMessage(Constant.SUCCESS_MESSAGE)
                .build());
    }

    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventResponse> updateEvent(@ModelAttribute EventRequest request) {
        log.info("Updating event with ID: {}", request);
        ResponseEntity<EventResponse> response = eventService.updateEvent(request);
        return response;
    }

    @PostMapping("/delete")
    public ResponseEntity<EventResponse> deleteEvent(@RequestBody GetEventRequest request) {
        eventService.deleteEvent(request);
        return ResponseEntity.ok(EventResponse.builder()
                .data(null)
                .responseCode(Constant.SUCCESS)
                .responseMessage(Constant.EVENT_DELETED_SUCCESSFULLY)
                .build());
    }
} 