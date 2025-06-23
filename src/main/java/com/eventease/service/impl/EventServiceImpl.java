package com.eventease.service.impl;

import com.eventease.common.Constant;
import com.eventease.domain.entity.Event;
import com.eventease.domain.entity.User;
import com.eventease.dto.EventRequest;
import com.eventease.dto.EventResponse;
import com.eventease.dto.EventResponseDTO;
import com.eventease.dto.GetEventRequest;
import com.eventease.repository.EventRepository;
import com.eventease.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    // Define a formatter
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Value("${event.image.path}")
    String configuredPath;
    @Value("${event.image.ipaddress}")
    String ipaddress;

    @Override
    @Transactional
    public Event createEvent(EventRequest request) {
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setLocation(request.getLocation());
        event.setEventDate(request.getEventDate());

        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            String originalFilename = request.getImageFile().getOriginalFilename();
            event.setFile(originalFilename);
            try {
                // Ensure directory exists
                Path dirPath = Paths.get(configuredPath);
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                }

                // Save file
                assert originalFilename != null;
                Path filePath = dirPath.resolve(originalFilename);
                Files.copy(request.getImageFile().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store file: " + originalFilename, e);
            }
        }

        return eventRepository.save(event);
    }

    @Override
    public EventResponseDTO getEventById(Long id) {
        Optional<Event> event = eventRepository.findById(id);
        Event event1 = event.get();
        return EventResponseDTO.builder()
                .id(event1.getId())
                .title(event1.getTitle())
                .description(event1.getDescription())
                .startDateTime(event1.getStartTime())
                .endDateTime(event1.getEndTime())
                .location(event1.getLocation())
                .eventDate(event1.getEventDate())
                .status(event1.getStatus().name())
                .isPublic(event1.isPublic())
                .imageUrl(ipaddress + event1.getFile())
                .build();
    }

//    @Override
//    public List<Event> getAllEvents() {
//        return eventRepository.findAll();
//    }
     @Override
     public List<EventResponseDTO> getAllEvents() {
         List<Event> events = eventRepository.findAll();



         return events.stream().map(event -> EventResponseDTO.builder()
             .id(event.getId())
             .title(event.getTitle())
             .description(event.getDescription())
             .startDateTime(event.getStartTime())
             .endDateTime(event.getEndTime())
             .location(event.getLocation())
             .eventDate(event.getEventDate())
             .status(event.getStatus().name())
             .isPublic(event.isPublic())
             .imageUrl(ipaddress + event.getFile()) // 👈 this is the image URL
             .build()
     ).collect(Collectors.toList());
 }

    @Override
    @Transactional
    public ResponseEntity<EventResponse> updateEvent(EventRequest request) {
        Optional<Event> eventOpt = eventRepository.findById((long) request.getId());
//                .orElseThrow(() -> new RuntimeException("Event not found with id: " + request.getId()));
        if(!eventOpt.isPresent()) {
            return ResponseEntity.badRequest().body(EventResponse.builder()
                    .data(null)
                    .responseCode(Constant.FAILURE)
                    .responseMessage(Constant.EVENT_NOT_FOUND)
                    .build());
        }
        Event event = eventOpt.get();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setLocation(request.getLocation());
        event.setEventDate(request.getEventDate());
        event.setUpdatedAt(String.valueOf(LocalDateTime.now()));

        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            String originalFilename = request.getImageFile().getOriginalFilename();
            event.setFile(originalFilename);
            try {
                // Ensure directory exists
                Path dirPath = Paths.get(configuredPath);
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                }

                // Save file
                assert originalFilename != null;
                Path filePath = dirPath.resolve(originalFilename);
                Files.copy(request.getImageFile().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store file: " + originalFilename, e);
            }
        }

         eventRepository.save(event);
        return ResponseEntity.ok(EventResponse.builder()
                .data(null)
                .responseCode(Constant.SUCCESS)
                .responseMessage(Constant.EVENT_UPDATED_SUCCESSFULLY)
                .build());
    }

    @Override
    @Transactional
    public void deleteEvent(GetEventRequest request) {
        Event event = eventRepository.findById(Long.valueOf(request.getId()))
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + request.getId()));
        eventRepository.delete(event);
    }

    @Override
    public Page<Event> findPublicEvents(Pageable pageable) {
        return eventRepository.findByIsPublicTrueAndStatusNot(Event.EventStatus.CANCELLED, pageable);
    }

    @Override
    public Page<Event> findEventsByOrganizer(User organizer, Pageable pageable) {
        return eventRepository.findByOrganizer(organizer, pageable);
    }

    @Override
    public Page<Event> findEventsByAttendee(User attendee, Pageable pageable) {
        return eventRepository.findByAttendeesContaining(attendee, pageable);
    }

    @Override
    public List<Event> findEventsBetweenDates(LocalDateTime start, LocalDateTime end) {
        return eventRepository.findEventsBetweenDates(start, end);
    }

    @Override
    public Page<Event> searchEvents(String searchTerm, Pageable pageable) {
        return eventRepository.searchPublicEvents(searchTerm, pageable);
    }

    

   

    private LocalDateTime parseFlexibleDateTime(String input) {
        if (input.length() == 10) { // Format is yyyy-MM-dd
            return LocalDate.parse(input, dateFormatter).atStartOfDay();
        } else {
            return LocalDateTime.parse(input, dateTimeFormatter);
        }
    }

    public static String getCurrentTimeStampForOBD() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(new Date());
    }
} 