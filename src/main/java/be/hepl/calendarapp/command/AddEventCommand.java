// be/hepl/calendarapp/undo/commands/AddEventCommand.java
package be.hepl.calendarapp.command;

import be.hepl.calendarapp.model.CalendarRepository;
import be.hepl.calendarapp.model.Event;

public class AddEventCommand extends AbstractCommand {
    private final CalendarRepository repo;
    private final Event event;

    public AddEventCommand(CalendarRepository repo, Event event){
        super("Ajouter événement");
        this.repo = repo;
        this.event = event;
    }

    @Override public void execute() { repo.addEvent(event); }

    @Override public void undo() { repo.removeEvent(event); }
}
