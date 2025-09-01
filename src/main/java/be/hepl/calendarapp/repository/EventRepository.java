package be.hepl.calendarapp.repository;


import be.hepl.calendarapp.model.Event;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EventRepository {
    void save(Event event);
    void delete(UUID id);
    List<Event> findByDate(LocalDate date);
}