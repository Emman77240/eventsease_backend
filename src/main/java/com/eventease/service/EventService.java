package com.eventease.service;

import com.eventease.domain.entity.Event;
import com.eventease.domain.entity.User;
import com.eventease.dto.EventRequest;
import com.eventease.dto.EventResponse;
import com.eventease.dto.EventResponseDTO;

import com.eventease.dto.GetEventRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    Event createEvent(EventRequest request);
    EventResponseDTO getEventById(Long id);
    List<EventResponseDTO> getAllEvents();
    ResponseEntity<EventResponse> updateEvent(EventRequest request);
    void deleteEvent(GetEventRequest request);
    Page<Event> findPublicEvents(Pageable pageable);
    Page<Event> findEventsByOrganizer(User organizer, Pageable pageable);
    Page<Event> findEventsByAttendee(User attendee, Pageable pageable);
    List<Event> findEventsBetweenDates(LocalDateTime start, LocalDateTime end);
    Page<Event> searchEvents(String searchTerm, Pageable pageable);
} 