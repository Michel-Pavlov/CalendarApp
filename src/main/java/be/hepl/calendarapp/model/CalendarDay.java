package be.hepl.calendarapp.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CalendarDay {
    private final LocalDate date;
    private final List<Event> events = new ArrayList<>();
    private String manualColorHex;      // null = pas de couleur manuelle
    private double dayCost = 0.0;       // << coût par jour, éditable

    public CalendarDay(LocalDate date) { this.date = date; }

    public LocalDate date() { return date; }
    public List<Event> events() { return events; }

    public void addEvent(Event e) { events.add(e); }

    public void setManualColorHex(String hex) { manualColorHex = hex; }
    public String manualColorHex() { return manualColorHex; }

    public int totalHours() {
        return events.stream().mapToInt(Event::durationHours).sum();
    }

    // ----- coût par jour (ne dépend plus des événements) -----
    public double dayCost() { return dayCost; }
    public void setDayCost(double cost) { this.dayCost = Math.max(0.0, cost); }

    // pour compatibilité avec affichage existant
    public double totalCost() { return dayCost(); }
}
