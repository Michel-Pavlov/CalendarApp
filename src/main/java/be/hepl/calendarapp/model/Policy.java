package be.hepl.calendarapp.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Policy {
    private final String name;
    private final List<Rule> rules = new ArrayList<>();
    private String defaultColor = "#FFFFFF";

    public Policy(String name) { this.name = name; }

    public String name() { return name; }
    public List<Rule> rules() { return rules; }
    public String defaultColor() { return defaultColor; }
    public void setDefaultColor(String hex) { this.defaultColor = hex; }

    public String colorFor(CalendarDay day) {
        double valueHours  = day.totalHours();
        double valueEvents = day.events().size();
        double valueCost   = day.totalCost();

        var copy = new ArrayList<>(rules);
        Collections.sort(copy); // appliquer par priorité

        for (Rule r : copy) {
            double v = switch (r.metric) {
                case TOTAL_HOURS -> valueHours;
                case EVENTS_COUNT -> valueEvents;
                case TOTAL_COST -> valueCost;
            };
            if (r.matches(v)) return r.colorHex;
        }
        return defaultColor;
    }
}
