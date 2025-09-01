package be.hepl.calendarapp.command;

import be.hepl.calendarapp.model.CalendarRepository;

import java.time.LocalDate;

public class SetDayCostCommand extends AbstractCommand {
    private final CalendarRepository repo;
    private final LocalDate date;
    private final double newCost;
    private double oldCost;

    public SetDayCostCommand(CalendarRepository repo, LocalDate date, double newCost) {
        super("Coût du jour");
        this.repo = repo; this.date = date; this.newCost = Math.max(0.0, newCost);
    }

    @Override public void execute() { oldCost = repo.setDayCost(date, newCost); }
    @Override public void undo() { repo.setDayCost(date, oldCost); }
}
