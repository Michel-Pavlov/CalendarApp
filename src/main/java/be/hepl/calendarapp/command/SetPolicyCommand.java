package be.hepl.calendarapp.command;

import be.hepl.calendarapp.model.CalendarRepository;
import be.hepl.calendarapp.model.Policy;

public class SetPolicyCommand extends AbstractCommand {
    private final CalendarRepository repo;
    private final Policy newPolicy;
    private Policy oldPolicy;

    public SetPolicyCommand(CalendarRepository repo, Policy newPolicy){
        super("Changer policy");
        this.repo = repo; this.newPolicy = newPolicy;
    }

    @Override public void execute() {
        oldPolicy = repo.model().policy();   // snapshot
        repo.setPolicy(newPolicy);
    }

    @Override public void undo() { repo.setPolicy(oldPolicy); }
}
