package be.hepl.calendarapp.strategy;


import be.hepl.calendarapp.model.CalendarDay;

@FunctionalInterface
public interface ColoringStrategy {
    String colorFor(CalendarDay day);
}