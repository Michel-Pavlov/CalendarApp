package be.hepl.calendarapp.command;

public abstract class AbstractCommand implements Command {
    private final String name;

    protected AbstractCommand(String name) { this.name = name; }

    @Override public String name() { return name; }
}
