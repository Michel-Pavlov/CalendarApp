package be.hepl.calendarapp.model;

import java.time.LocalDate;

public interface CalendarListener {
    void onDayUpdated(LocalDate date);
    default void onPolicyChanged() {}
}