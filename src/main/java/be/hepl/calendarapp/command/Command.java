package be.hepl.calendarapp.command;

public interface Command {
        String name();
        void execute();
        void undo();
        default void redo() { execute(); }
}