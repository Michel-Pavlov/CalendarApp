package be.hepl.calendarapp.model;

import be.hepl.calendarapp.strategy.ColoringStrategy;
import be.hepl.calendarapp.strategy.OvertimeColoring;

public record ColoringPolicy(String name, ColoringStrategy strategy) {
    public static final ColoringPolicy DEFAULT_OVERTIME =
        new ColoringPolicy("Heures > 8 = Rouge", new OvertimeColoring(8));
}