package be.hepl.calendarapp.model;

import java.time.LocalDate;

public class CalendarRepository {
    private final CalendarModel model;

    public CalendarRepository(CalendarModel model){ this.model = model; }

    public CalendarModel model(){ return model; }

    public void addEvent(Event e){
        model.getDay(e.date()).addEvent(e);
        model.fireDayPublic(e.date());
    }

    public void removeEvent(Event e){
        model.getDay(e.date()).events().remove(e);
        model.fireDayPublic(e.date());
    }

    public String setManualColor(LocalDate date, String hex){
        CalendarDay d = model.getDay(date);
        String old = d.manualColorHex();
        d.setManualColorHex(hex);
        model.fireDayPublic(date);
        return old;
    }

    // ----- coût par jour -----
    public double setDayCost(LocalDate date, double newCost){
        CalendarDay d = model.getDay(date);
        double old = d.dayCost();
        d.setDayCost(newCost);
        model.fireDayPublic(date);
        return old;
    }

    public void setPolicy(Policy p){
        model.setPolicyInternal(p);
    }
}
