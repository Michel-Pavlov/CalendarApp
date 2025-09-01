package be.hepl.calendarapp.view;

import be.hepl.calendarapp.model.*;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.Comparator;

public class PolicyEditorDialog extends Dialog<Policy> {
    public PolicyEditorDialog(Policy initial){
        setTitle("Éditeur de policy");
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // === Formulaire de règle ===
        ComboBox<Metric> metric = new ComboBox<>();
        metric.getItems().addAll(Metric.values());
        metric.getSelectionModel().select(Metric.TOTAL_HOURS);

        ComboBox<ComparatorOp> op = new ComboBox<>();
        op.getItems().addAll(ComparatorOp.values());
        op.getSelectionModel().select(ComparatorOp.GT);

        TextField a = new TextField("8");
        TextField b = new TextField("0");
        b.setDisable(true);
        op.valueProperty().addListener((o,ov,nv)-> b.setDisable(nv != ComparatorOp.BETWEEN));

        ColorPicker color = new ColorPicker(Color.web("#FFCDD2"));
        Spinner<Integer> prio = new Spinner<>(1, 999, 10);

        Button add = new Button("Ajouter");
        Button update = new Button("Mettre à jour");
        Button duplicate = new Button("Dupliquer");
        Button remove = new Button("Supprimer");
        Button up = new Button("Monter");
        Button down = new Button("Descendre");

        // === Table des règles ===
        TableView<Rule> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Rule,String> c1 = new TableColumn<>("Métrique");
        c1.setCellValueFactory(v -> new ReadOnlyStringWrapper(v.getValue().metric.name()));
        TableColumn<Rule,String> c2 = new TableColumn<>("Op");
        c2.setCellValueFactory(v -> new ReadOnlyStringWrapper(v.getValue().op.name()));
        TableColumn<Rule,String> c3 = new TableColumn<>("A");
        c3.setCellValueFactory(v -> new ReadOnlyStringWrapper(trimZeros(v.getValue().a)));
        TableColumn<Rule,String> c4 = new TableColumn<>("B");
        c4.setCellValueFactory(v -> new ReadOnlyStringWrapper(trimZeros(v.getValue().b)));
        TableColumn<Rule,String> c5 = new TableColumn<>("Couleur");
        c5.setCellValueFactory(v -> new ReadOnlyStringWrapper(v.getValue().colorHex));
        TableColumn<Rule,String> c6 = new TableColumn<>("Priorité");
        c6.setCellValueFactory(v -> new ReadOnlyStringWrapper(Integer.toString(v.getValue().priority)));

        table.getColumns().addAll(c1,c2,c3,c4,c5,c6);

        // === Working copy de la policy ===
        Policy working = new Policy(initial == null ? "Custom" : initial.name());
        if (initial != null) {
            working.setDefaultColor(initial.defaultColor());
            working.rules().addAll(initial.rules()); // shallow copy suffisant (Rule = data object)
        }

        // normalise les priorités si doublons/vides
        normalizePriorities(working);

        table.getItems().setAll(working.rules().stream()
                .sorted(Comparator.comparingInt(r -> r.priority))
                .toList());

        // === Actions ===
        add.setOnAction(e -> {
            Rule r = new Rule(
                    metric.getValue(),
                    op.getValue(),
                    parseDouble(a.getText(), 0),
                    parseDouble(b.getText(), 0),
                    toHex(color.getValue()),
                    prio.getValue()
            );
            working.rules().add(r);
            refreshTable(table, working);
            table.getSelectionModel().select(r);
        });

        update.setOnAction(e -> {
            Rule sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            sel.metric = metric.getValue();
            sel.op = op.getValue();
            sel.a = parseDouble(a.getText(), sel.a);
            sel.b = parseDouble(b.getText(), sel.b);
            sel.colorHex = toHex(color.getValue());
            sel.priority = prio.getValue();
            refreshTable(table, working);
            table.getSelectionModel().select(sel);
        });

        duplicate.setOnAction(e -> {
            Rule sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            Rule copy = new Rule(sel.metric, sel.op, sel.a, sel.b, sel.colorHex, sel.priority + 1);
            working.rules().add(copy);
            normalizePriorities(working);
            refreshTable(table, working);
            table.getSelectionModel().select(copy);
        });

        remove.setOnAction(e -> {
            Rule sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            working.rules().remove(sel);
            refreshTable(table, working);
        });

        up.setOnAction(e -> moveSelected(table, working, -1));
        down.setOnAction(e -> moveSelected(table, working, +1));

        // remplir formulaire quand on sélectionne une règle
        table.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if (nv == null) return;
            metric.getSelectionModel().select(nv.metric);
            op.getSelectionModel().select(nv.op);
            a.setText(trimZeros(nv.a));
            b.setText(trimZeros(nv.b));
            b.setDisable(nv.op != ComparatorOp.BETWEEN);
            color.setValue(Color.web(nv.colorHex));
            prio.getValueFactory().setValue(nv.priority);
        });

        // double-clic = charger la règle dans le formulaire (déjà géré par listener)
        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Rule nv = table.getSelectionModel().getSelectedItem();
                if (nv != null) {
                    metric.getSelectionModel().select(nv.metric);
                    op.getSelectionModel().select(nv.op);
                    a.setText(trimZeros(nv.a));
                    b.setText(trimZeros(nv.b));
                    b.setDisable(nv.op != ComparatorOp.BETWEEN);
                    color.setValue(Color.web(nv.colorHex));
                    prio.getValueFactory().setValue(nv.priority);
                }
            }
        });

        // === Couleur par défaut ===
        ColorPicker defaultColor = new ColorPicker(Color.web(working.defaultColor()));
        defaultColor.setOnAction(e -> working.setDefaultColor(toHex(defaultColor.getValue())));

        // === Layout ===
        GridPane form = new GridPane();
        form.setHgap(8); form.setVgap(8); form.setPadding(new Insets(10));
        int r = 0;
        form.addRow(r++, new Label("Métrique:"), metric, new Label("Opérateur:"), op);
        form.addRow(r++, new Label("Seuil A:"), a, new Label("Seuil B:"), b);
        form.addRow(r++, new Label("Couleur:"), color, new Label("Priorité:"), prio);

        HBox actions = new HBox(8, add, update, duplicate, remove, new Separator(), up, down);
        actions.setPadding(new Insets(0,10,0,10));

        VBox root = new VBox(10,
                form,
                actions,
                new HBox(8, new Label("Couleur par défaut:"), defaultColor),
                new Label("Règles (appliquées par priorité croissante, première qui matche gagne) :"),
                table
        );
        root.setPadding(new Insets(12));
        getDialogPane().setContent(root);

        setResultConverter(bt -> bt == ButtonType.OK ? working : null);
    }

    // === Utils ===
    private static void refreshTable(TableView<Rule> table, Policy working){
        // trie par priorité et recharge
        working.rules().sort(Comparator.comparingInt(r -> r.priority));
        table.getItems().setAll(working.rules());
    }

    private static void moveSelected(TableView<Rule> table, Policy working, int delta){
        int idx = table.getSelectionModel().getSelectedIndex();
        if (idx < 0) return;

        working.rules().sort(Comparator.comparingInt(r -> r.priority));
        int newIdx = idx + delta;
        if (newIdx < 0 || newIdx >= working.rules().size()) return;

        Rule a = working.rules().get(idx);
        Rule b = working.rules().get(newIdx);
        int p = a.priority;
        a.priority = b.priority;
        b.priority = p;

        refreshTable(table, working);
        table.getSelectionModel().select(b);
    }

    private static void normalizePriorities(Policy p){
        p.rules().sort(Comparator.comparingInt(r -> r.priority));
        int pr = 10;
        for (Rule r : p.rules()){
            if (r.priority <= 0) r.priority = pr;
            pr += 10;
        }
    }

    private static String toHex(Color c){
        return String.format("#%02X%02X%02X",
                (int)Math.round(c.getRed()*255),
                (int)Math.round(c.getGreen()*255),
                (int)Math.round(c.getBlue()*255));
    }
    private static double parseDouble(String s, double def){
        try { return Double.parseDouble(s.replace(",", ".")); } catch (Exception e) { return def; }
    }
    private static String trimZeros(double v){
        if (Math.abs(v - Math.rint(v)) < 1e-9) return Integer.toString((int)Math.rint(v));
        return Double.toString(v);
    }
}
