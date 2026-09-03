package model;

public class Mission {
    private int id;
    private String title;
    private String date;
    private String description;
    private int facilityId;

    public Mission(int id, String title, String date, String description, int facilityId) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.description = description;
        this.facilityId = facilityId;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public int getFacilityId() {
        return facilityId;
    }
}