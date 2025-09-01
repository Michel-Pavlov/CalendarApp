// be/hepl/calendarapp/model/Event.java
package be.hepl.calendarapp.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record Event(UUID id, String title, LocalDate date,
                    LocalTime start, LocalTime end,
                    Category category, String notes) {

    public int durationHours() {
        int h = Math.max(0, end.getHour() - start.getHour());
        return Math.min(h, 24);
    }
    public double categoryRate(){ return category.hourlyRate; }
}
