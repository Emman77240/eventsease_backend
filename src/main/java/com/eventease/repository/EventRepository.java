package com.eventease.repository;

import com.eventease.domain.entity.Event;
import com.eventease.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByIsPublicTrueAndStatusNot(Event.EventStatus status, Pageable pageable);
    
    Page<Event> findByOrganizer(User organizer, Pageable pageable);
    
    Page<Event> findByAttendeesContaining(User attendee, Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE e.startTime >= :start AND e.endTime <= :end")
    List<Event> findEventsBetweenDates(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT e FROM Event e WHERE e.isPublic = true AND " +
           "(LOWER(e.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Event> searchPublicEvents(String searchTerm, Pageable pageable);
} 