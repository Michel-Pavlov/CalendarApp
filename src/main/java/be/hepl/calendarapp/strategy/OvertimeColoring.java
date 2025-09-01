package be.hepl.calendarapp.strategy;


import be.hepl.calendarapp.model.CalendarDay;

public class OvertimeColoring implements ColoringStrategy {
    private final int thresholdHours;

    public OvertimeColoring(int thresholdHours){
        this.thresholdHours = thresholdHours;
    }

    @Override
    public String colorFor(CalendarDay day) {
        return day.totalHours() > thresholdHours ? "#FFCDD2" : "#FFFFFF"; // light red
    }
}