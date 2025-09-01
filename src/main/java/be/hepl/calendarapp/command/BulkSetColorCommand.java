package be.hepl.calendarapp.command;

import be.hepl.calendarapp.model.CalendarRepository;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class BulkSetColorCommand extends AbstractCommand {
    private final CalendarRepository repo;
    private final Set<LocalDate> dates;
    private final String newHex;
    private final Map<LocalDate, String> oldByDate = new LinkedHashMap<>();

    public BulkSetColorCommand(CalendarRepository repo, Set<LocalDate> dates, String newHex){
        super("Couleur sélection");
        this.repo = repo; this.dates = dates; this.newHex = newHex;
    }

    @Override public void execute() {
        oldByDate.clear();
        for (LocalDate d : dates){
            String old = repo.setManualColor(d, newHex);
            oldByDate.put(d, old);
        }
    }

    @Override public void undo() {
        for (Map.Entry<LocalDate, String> e : oldByDate.entrySet()){
            repo.setManualColor(e.getKey(), e.getValue());
        }
    }
}
