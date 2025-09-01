package be.hepl.calendarapp.presenter;

import be.hepl.calendarapp.command.*;
import be.hepl.calendarapp.model.*;
import be.hepl.calendarapp.storage.PolicyStorage;
import be.hepl.calendarapp.view.EventEditorDialog;
import be.hepl.calendarapp.view.MainView;
import be.hepl.calendarapp.view.PolicyEditorDialog;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Set;

public class CalendarPresenter {
    private final CalendarModel model;
    private final CalendarRepository repo;
    private final CommandManager commands = new CommandManager();
    private final MainView view;
    private final PolicyStorage policyStorage =
            new PolicyStorage(Path.of(System.getProperty("user.home"), ".calendarapp", "policy.json"));

    public CalendarPresenter(CalendarModel model, MainView view){
        this.model = model;
        this.repo = new CalendarRepository(model);
        this.view = view;
        model.addListener(view);

        Policy loaded = policyStorage.loadOrDefault(CalendarModel.defaultPolicy());
        commands.execute(new SetPolicyCommand(repo, loaded));
        view.refreshAll(model);

        // Ouvrir le dialog "Nouvel événement"
        view.setOnOpenAddEvent(day -> {
            EventEditorDialog dlg = new EventEditorDialog(day);
            var res = dlg.showAndWait();
            res.ifPresent(event -> {
                commands.execute(new AddEventCommand(repo, event));
                view.refreshAll(model);
            });
        });

        // Couleur d’un seul jour
        view.setOnColorSingle((date, hex) -> {
            commands.execute(new SetDayColorCommand(repo, date, hex));
            view.refreshAll(model);
        });

        // Coloration en masse
        view.setOnBulkColor((Set<LocalDate> dates, String hex) -> {
            commands.execute(new BulkSetColorCommand(repo, dates, hex));
            view.refreshAll(model);
        });

        // Coût du jour
        view.setOnSetDayCost((date, cost) -> {
            commands.execute(new SetDayCostCommand(repo, date, cost));
            view.refreshAll(model);
        });

        // Undo / Redo
        view.setOnUndo(() -> { commands.undo(); view.refreshAll(model); });
        view.setOnRedo(() -> { commands.redo(); view.refreshAll(model); });

        view.setOnPolicyChange(choice -> {
            switch (choice) {
                case "Aucune" -> {
                    commands.execute(new SetPolicyCommand(repo, CalendarModel.nonePolicy()));
                    view.refreshAll(model);
                }
                case "Heures > 8" -> {
                    commands.execute(new SetPolicyCommand(repo, CalendarModel.defaultPolicy()));
                    view.refreshAll(model);
                }
                default -> { // Personnalisée
                    PolicyEditorDialog dlg = new PolicyEditorDialog(loaded);
                    var res = dlg.showAndWait();
                    if (res.isPresent() && res.get() != null) {
                        commands.execute(new SetPolicyCommand(repo, res.get()));
                        policyStorage.save(loaded);
                        view.refreshAll(model);
                    }
                }
            }
        });

        view.renderMonth(model);
        view.refreshAll(model);
    }

    public CalendarModel getModel(){ return model; }
}
