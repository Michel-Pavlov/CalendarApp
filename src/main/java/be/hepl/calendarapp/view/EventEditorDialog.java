package be.hepl.calendarapp.view;

import be.hepl.calendarapp.model.Category;
import be.hepl.calendarapp.model.Event;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class EventEditorDialog extends Dialog<Event> {

    public EventEditorDialog(LocalDate defaultDate){
        setTitle("Nouvel événement");
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField title = new TextField();
        title.setPromptText("Titre");

        DatePicker date = new DatePicker(defaultDate == null ? LocalDate.now() : defaultDate);

        TextField start = new TextField("09:00");
        TextField end   = new TextField("11:00");
        Tooltip.install(start, new Tooltip("Format HH:MM"));
        Tooltip.install(end, new Tooltip("Format HH:MM"));

        ComboBox<Category> category = new ComboBox<>();
        category.getItems().addAll(Category.values());
        category.getSelectionModel().select(Category.WORK);

        TextArea notes = new TextArea();
        notes.setPromptText("Notes (optionnel)");
        notes.setPrefRowCount(3);

        GridPane gp = new GridPane();
        gp.setHgap(10); gp.setVgap(8); gp.setPadding(new Insets(12));
        int r = 0;
        gp.addRow(r++, new Label("Titre:"), title);
        gp.addRow(r++, new Label("Date:"), date);
        gp.addRow(r++, new Label("Début:"), start, new Label("Fin:"), end);
        gp.addRow(r++, new Label("Catégorie:"), category);
        gp.addRow(r++, new Label("Notes:"), notes);

        getDialogPane().setContent(gp);

        setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            String t = title.getText().isBlank() ? "Événement" : title.getText().trim();
            LocalDate d = date.getValue() == null ? LocalDate.now() : date.getValue();
            LocalTime s = parseTime(start.getText(), LocalTime.of(9,0));
            LocalTime e = parseTime(end.getText(),   LocalTime.of(11,0));
            if (!e.isAfter(s)) e = s.plusHours(1);
            return new Event(UUID.randomUUID(), t, d, s, e, category.getValue(), notes.getText());
        });
    }

    private static LocalTime parseTime(String txt, LocalTime def){
        try {
            String[] p = txt.trim().split("[:hH]");
            int hh = Integer.parseInt(p[0]);
            int mm = (p.length>1) ? Integer.parseInt(p[1]) : 0;
            if (hh<0 || hh>23 || mm<0 || mm>59) return def;
            return LocalTime.of(hh, mm);
        } catch(Exception ex){
            return def;
        }
    }
}
