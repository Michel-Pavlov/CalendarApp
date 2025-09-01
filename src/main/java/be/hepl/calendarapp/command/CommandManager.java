// be/hepl/calendarapp/undo/CommandManager.java
package be.hepl.calendarapp.command;

import java.util.ArrayDeque;
import java.util.Deque;

public class CommandManager {
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void execute(Command c){
        c.execute();
        undoStack.push(c);
        redoStack.clear();
    }

    public boolean canUndo(){ return !undoStack.isEmpty(); }
    public boolean canRedo(){ return !redoStack.isEmpty(); }

    public void undo(){
        if (undoStack.isEmpty()) return;
        Command c = undoStack.pop();
        c.undo();
        redoStack.push(c);
    }

    public void redo(){
        if (redoStack.isEmpty()) return;
        Command c = redoStack.pop();
        c.redo();
        undoStack.push(c);
    }

    public void clear(){
        undoStack.clear(); redoStack.clear();
    }
}
