module be.hepl.calendarapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens be.hepl.calendarapp to javafx.fxml;
    exports be.hepl.calendarapp.app;
}