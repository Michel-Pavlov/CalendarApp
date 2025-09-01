package be.hepl.calendarapp.model;

import java.time.LocalDate;
import java.util.*;

public class CalendarModel {
    private final Map<LocalDate, CalendarDay> days = new HashMap<>();
    private final List<CalendarListener> listeners = new ArrayList<>();
    private Policy policy = defaultPolicy(); // preset par défaut

    public CalendarDay getDay(LocalDate date) {
        return days.computeIfAbsent(date, CalendarDay::new);
    }

    public void addListener(CalendarListener l) { listeners.add(l); }

    // ==== Hooks pour repo/commandes (package-private) ====
    void fireDayPublic(LocalDate date){
        for (CalendarListener l : listeners) l.onDayUpdated(date);
    }
    void setPolicyInternal(Policy p){
        this.policy = (p == null ? defaultPolicy() : p);
        listeners.forEach(CalendarListener::onPolicyChanged);
    }

    public Policy policy(){ return policy; }

    public String effectiveColorHex(CalendarDay day) {
        if (day.manualColorHex() != null) return day.manualColorHex();
        return policy.colorFor(day);
    }

    // === Presets utilitaires ===
    public static Policy defaultPolicy() {
        Policy p = new Policy("Heures > 8");
        p.rules().add(new Rule(Metric.TOTAL_HOURS, ComparatorOp.GT, 8, 0, "#FFCDD2", 10));
        p.setDefaultColor("#FFFFFF");
        return p;
    }

    public static Policy nonePolicy() {
        Policy p = new Policy("Aucune");
        p.setDefaultColor("#FFFFFF");
        return p;
    }
}
