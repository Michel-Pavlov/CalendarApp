package be.hepl.calendarapp.repository;

import be.hepl.calendarapp.model.Event;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


public class InMemoryEventRepository implements EventRepository {
    private final Map<UUID, Event> store = new HashMap<>();

    @Override
    public void save(Event event) {
        store.put(event.id(), event);
    }

    @Override
    public void delete(UUID id) {
        store.remove(id);
    }

    @Override
    public List<Event> findByDate(LocalDate date) {
        return store.values().stream().filter(e -> e.date().equals(date)).collect(Collectors.toList());
    }
}