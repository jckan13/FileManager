package model;

public class Brief {
    private int id;
    private String title;
    private String date;
    private String text;
    private int missionId;

    public Brief(int id, String title, String date, String text, int missionId) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.text = text;
        this.missionId = missionId;
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

    public String getText() {
        return text;
    }

    public int getMissionId() {
        return missionId;
    }
}