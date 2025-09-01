package be.hepl.calendarapp.model;

public enum Category {
    MEETING("#90CAF9", 25.0),
    WORK("#A5D6A7", 30.0),
    PERSONAL("#FFE082", 0.0),
    OTHER("#E0E0E0", 0.0);

    public final String colorHex;
    public final double hourlyRate;

    Category(String colorHex, double hourlyRate) {
        this.colorHex = colorHex;
        this.hourlyRate = hourlyRate;
    }
}
