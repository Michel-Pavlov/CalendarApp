package be.hepl.calendarapp.view;

import be.hepl.calendarapp.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;

public class MainView implements CalendarListener {
    private final BorderPane root = new BorderPane();
    private final GridPane monthGrid = new GridPane();
    private final Map<LocalDate, StackPane> dayCells = new HashMap<>();
    private final Set<LocalDate> selection = new LinkedHashSet<>();

    private BiConsumer<LocalDate, String> onQuickAdd;
    private TriConsumer<LocalDate, String, Integer> onAddEvent; // (gardé pour compat) - non utilisé
    private Runnable onUndo;
    private Runnable onRedo;
    private java.util.function.Consumer<String> onPolicyChange;
    private BiConsumer<Set<LocalDate>, String> onBulkColor;
    private BiConsumer<LocalDate, String> onColorSingle;
    private java.util.function.Consumer<LocalDate> onOpenAddEvent; // << ouvrir le dialog événement
    private java.util.function.BiConsumer<LocalDate, Double> onSetDayCost; // << changer le coût du jour

    private LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);

    public MainView(){
        buildToolbar();
        buildMonthGrid();
    }

    public Pane getRoot(){ return root; }

    private void buildToolbar(){
        ToolBar tb = new ToolBar();

        DatePicker monthPicker = new DatePicker(LocalDate.now());
        monthPicker.setShowWeekNumbers(false);
        monthPicker.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null){
                firstOfMonth = newV.withDayOfMonth(1);
                renderMonth(null);
            }
        });

        Button add = new Button("Ajouter un événement… (jour sélectionné)");
        add.setOnAction(e -> {
            LocalDate target = selection.isEmpty() ? LocalDate.now() : selection.iterator().next();
            if (onOpenAddEvent != null) onOpenAddEvent.accept(target);
        });

        ChoiceBox<String> policy = new ChoiceBox<>();
        policy.getItems().addAll("Aucune", "Heures > 8", "Personnalisée…");
        policy.getSelectionModel().select("Heures > 8");
        policy.getSelectionModel().selectedItemProperty().addListener((o,ov,nv)->{
            if (onPolicyChange != null) onPolicyChange.accept(nv);
        });

        Button undo = new Button("Undo");
        Button redo = new Button("Redo");
        undo.setOnAction(e -> { if (onUndo != null) onUndo.run(); });
        redo.setOnAction(e -> { if (onRedo != null) onRedo.run(); });

        ColorPicker selPicker = new ColorPicker();
        Button applySel = new Button("Appliquer à la sélection");
        Button clearSel = new Button("Désélectionner tout");

        applySel.setOnAction(e -> {
            if (onBulkColor != null && !selection.isEmpty()){
                String hex = toHex(selPicker.getValue());
                onBulkColor.accept(new LinkedHashSet<>(selection), hex);
            }
        });
        clearSel.setOnAction(e -> clearSelection());

        tb.getItems().addAll(new Label("Mois:"), monthPicker,
                new Separator(), add,
                new Separator(), new Label("Coloration auto:"), policy,
                new Separator(), undo, redo,
                new Separator(), new Label("Sélection:"), selPicker, applySel, clearSel);

        root.setTop(tb);
    }

    private void buildMonthGrid(){
        monthGrid.setHgap(6);
        monthGrid.setVgap(6);
        monthGrid.setPadding(new Insets(12));
        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(100.0/7);
        for (int i=0;i<7;i++) monthGrid.getColumnConstraints().add(cc);
        root.setCenter(monthGrid);
    }

    public void renderMonth(CalendarModel model){
        monthGrid.getChildren().clear();
        dayCells.clear();
        selection.clear();

        int length = firstOfMonth.lengthOfMonth();
        int startCol = firstOfMonth.getDayOfWeek().getValue() % 7; // Lundi=1 → col 1
        int row = 1;

        String[] headers = {"Dim","Lun","Mar","Mer","Jeu","Ven","Sam"};
        for (int i=0;i<7;i++){
            Label h = new Label(headers[i]);
            h.setStyle("-fx-font-weight: bold;");
            monthGrid.add(h, i, 0);
        }

        int col = startCol;
        for (int i=1;i<=length;i++){
            LocalDate day = firstOfMonth.withDayOfMonth(i);
            StackPane cell = dayCell(day, model);
            monthGrid.add(cell, col, row);
            dayCells.put(day, cell);
            col++;
            if (col==7){ col=0; row++; }
        }
    }

    private StackPane dayCell(LocalDate day, CalendarModel model){
        StackPane cell = new StackPane();
        cell.setMinSize(130, 120);
        VBox box = new VBox(4);
        box.setPadding(new Insets(6));
        box.setAlignment(Pos.TOP_LEFT);

        Text dateText = new Text(day.format(DateTimeFormatter.ofPattern("d MMM")));
        dateText.setStyle("-fx-font-weight: bold;");

        Label info = new Label("");
        info.setWrapText(true);

        // coût du jour : clic pour modifier
        Hyperlink costLink = new Hyperlink("€ Modifier coût");
        costLink.setOnAction(e -> {
            TextInputDialog dlg = new TextInputDialog(String.format(Locale.US, "%.2f", model != null ? model.getDay(day).dayCost() : 0.0));
            dlg.setTitle("Coût du jour");
            dlg.setHeaderText("Définir le coût pour " + day);
            dlg.setContentText("Montant (€) :");
            dlg.showAndWait().ifPresent(txt -> {
                try {
                    double v = Double.parseDouble(txt.replace(",", "."));
                    if (onSetDayCost != null) onSetDayCost.accept(day, v);
                } catch(Exception ex){ /* ignore format */ }
            });
        });

        VBox eventsBox = new VBox(2);

        Button quickAdd = new Button("+");
        quickAdd.setOnAction(e -> {
            if (onOpenAddEvent != null) onOpenAddEvent.accept(day);
        });

        HBox header = new HBox(6, dateText, quickAdd);
        header.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(header, info, costLink, eventsBox);
        cell.getChildren().add(box);

        // Toggle sélection
        cell.setOnMouseClicked(e -> toggleSelect(day, cell));

        // Menu contextuel
        ContextMenu ctx = new ContextMenu();
        MenuItem addEvt = new MenuItem("Ajouter un événement…");
        addEvt.setOnAction(e -> { if (onOpenAddEvent != null) onOpenAddEvent.accept(day); });

        ColorPicker picker = new ColorPicker(Color.WHITE);
        CustomMenuItem colorItem = new CustomMenuItem(picker, false);
        picker.setOnAction(ev -> {
            String hex = toHex(picker.getValue());
            if (onColorSingle != null) onColorSingle.accept(day, hex);
        });

        MenuItem applyToSelection = new MenuItem("Appliquer cette couleur à la sélection");
        applyToSelection.setOnAction(ev -> {
            if (onBulkColor != null && !selection.isEmpty()){
                String hex = toHex(picker.getValue());
                onBulkColor.accept(new LinkedHashSet<>(selection), hex);
            }
        });

        MenuItem editCost = new MenuItem("Définir le coût du jour…");
        editCost.setOnAction(e -> costLink.fire());

        ctx.getItems().addAll(addEvt, new SeparatorMenuItem(),
                new MenuItem("Couleur du jour (ci-dessous)"), colorItem,
                applyToSelection,
                new SeparatorMenuItem(), editCost);
        cell.setOnContextMenuRequested(e -> ctx.show(cell, e.getScreenX(), e.getScreenY()));

        if (model != null) {
            paintCell(model.getDay(day), cell, info, eventsBox, model, dateText, costLink, header);
        }

        return cell;
    }

    private void paintCell(CalendarDay day, StackPane cell, Label info, VBox eventsBox, CalendarModel model,
                           Text dateText, Hyperlink costLink, HBox header){
        String bg = (model != null) ? model.effectiveColorHex(day) : day.manualColorHex();
        if (bg == null) bg = "#FFFFFF";
        cell.setStyle("-fx-background-color: " + bg + "; -fx-border-color: #E0E0E0;");

        // ----- Contraste auto du texte -----
        String textColor = contrastColor(bg); // "#000000" ou "#FFFFFF"
        String textStyle = "-fx-text-fill: " + textColor + ";";
        dateText.setStyle("-fx-font-weight: bold; " + textStyle);
        info.setStyle(textStyle);
        costLink.setStyle(textStyle);
        header.setStyle(textStyle);

        info.setText("Évts: " + day.events().size() + "  |  h: " + day.totalHours()
                + "  |  €: " + String.format(Locale.US, "%.2f", day.totalCost()));

        eventsBox.getChildren().clear();
        int shown = 0;
        for (var e : day.events()){
            if (shown >= 3) { eventsBox.getChildren().add(labelWithColor("…", textColor)); break; }
            Label lbl = labelWithColor("• " + e.title(), textColor);
            lbl.setStyle(lbl.getStyle() + "-fx-font-size: 11;");
            eventsBox.getChildren().add(lbl);
            shown++;
        }
    }

    private Label labelWithColor(String text, String colorHex){
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + colorHex + ";");
        return l;
    }

    private String contrastColor(String bgHex){
        // calcule luminance relative et choisit noir/blanc
        try {
            Color c = Color.web(bgHex);
            double r = c.getRed(), g = c.getGreen(), b = c.getBlue();
            double lum = 0.2126*r + 0.7152*g + 0.0722*b; // approx sRGB
            return lum > 0.6 ? "#000000" : "#FFFFFF";
        } catch(Exception e){
            return "#000000";
        }
    }

    private void toggleSelect(LocalDate day, StackPane cell){
        if (selection.remove(day)) applySelectionStyle(cell, false);
        else { selection.add(day); applySelectionStyle(cell, true); }
    }

    private void clearSelection(){
        selection.forEach(d -> {
            StackPane c = dayCells.get(d);
            if (c != null) applySelectionStyle(c, false);
        });
        selection.clear();
    }

    private void applySelectionStyle(StackPane cell, boolean selected){
        if (selected) {
            cell.setStyle(cell.getStyle() + "; -fx-border-color: #1976D2; -fx-border-width: 2;");
        } else {
            cell.setStyle(cell.getStyle().replaceAll("-fx-border-width:\\s*2;?", ""));
        }
    }

    private static String toHex(Color c){
        return String.format("#%02X%02X%02X",
                (int)Math.round(c.getRed()*255),
                (int)Math.round(c.getGreen()*255),
                (int)Math.round(c.getBlue()*255));
    }

    // === CalendarListener ===
    @Override public void onDayUpdated(LocalDate date) { /* géré via refreshAll */ }
    @Override public void onPolicyChanged() { /* idem */ }

    public void refreshAll(CalendarModel model){
        for (Map.Entry<LocalDate, StackPane> e : dayCells.entrySet()){
            CalendarDay day = model.getDay(e.getKey());
            Label info = null; VBox eventsBox = null; Text dateText = null; Hyperlink costLink = null; HBox header = null;
            StackPane cell = e.getValue();
            for (Node n : cell.getChildren()){
                if (n instanceof VBox vbox){
                    // header (HBox), info (Label), costLink (Hyperlink), eventsBox (VBox)
                    int idx = 0;
                    for (Node c : vbox.getChildren()){
                        if (idx == 0 && c instanceof HBox h) { header = h;
                            for (Node hChild : h.getChildren()) if (hChild instanceof Text t) dateText = t;
                        } else if (idx == 1 && c instanceof Label l) info = l;
                        else if (idx == 2 && c instanceof Hyperlink hl) costLink = hl;
                        else if (idx == 3 && c instanceof VBox vv) eventsBox = vv;
                        idx++;
                    }
                }
            }
            if (info != null && eventsBox != null && dateText != null && costLink != null && header != null) {
                paintCell(day, cell, info, eventsBox, model, dateText, costLink, header);
                applySelectionStyle(cell, selection.contains(e.getKey()));
            }
        }
    }

    // Callbacks wiring
    public void setOnQuickAdd(BiConsumer<LocalDate, String> onQuickAdd) { this.onQuickAdd = onQuickAdd; }
    public void setOnAddEvent(TriConsumer<LocalDate, String, Integer> onAddEvent) { this.onAddEvent = onAddEvent; }
    public void setOnUndo(Runnable onUndo) { this.onUndo = onUndo; }
    public void setOnRedo(Runnable onRedo) { this.onRedo = onRedo; }
    public void setOnPolicyChange(java.util.function.Consumer<String> onPolicyChange){ this.onPolicyChange = onPolicyChange; }
    public void setOnBulkColor(BiConsumer<Set<LocalDate>, String> onBulkColor){ this.onBulkColor = onBulkColor; }
    public void setOnColorSingle(BiConsumer<LocalDate, String> onColorSingle){ this.onColorSingle = onColorSingle; }
    public void setOnOpenAddEvent(java.util.function.Consumer<LocalDate> c){ this.onOpenAddEvent = c; }
    public void setOnSetDayCost(java.util.function.BiConsumer<LocalDate, Double> c){ this.onSetDayCost = c; }
}
