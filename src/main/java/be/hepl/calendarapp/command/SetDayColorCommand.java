package be.hepl.calendarapp.command;

import be.hepl.calendarapp.model.CalendarRepository;

import java.time.LocalDate;

public class SetDayColorCommand extends AbstractCommand {
    private final CalendarRepository repo;
    private final LocalDate date;
    private final String newHex;
    private String oldHex;

    public SetDayColorCommand(CalendarRepository repo, LocalDate date, String newHex){
        super("Couleur jour");
        this.repo = repo; this.date = date; this.newHex = newHex;
    }

    @Override public void execute() { oldHex = repo.setManualColor(date, newHex); }

    @Override public void undo() { repo.setManualColor(date, oldHex); }
}
