package be.hepl.calendarapp.app;

import be.hepl.calendarapp.model.CalendarModel;
import be.hepl.calendarapp.presenter.CalendarPresenter;
import be.hepl.calendarapp.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        CalendarModel model = new CalendarModel();
        MainView view = new MainView();
        CalendarPresenter presenter = new CalendarPresenter(model, view);

        Scene scene = new Scene(view.getRoot(), 1500, 700);
        stage.setTitle("Calendar-Tableur");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}