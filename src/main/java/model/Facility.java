package model;

public class Facility {
    private int id;
    private String name;
    private String abbreviation;

    public Facility(int id, String name, String abbreviation) {
        this.id = id;
        this.name = name;
        this.abbreviation = abbreviation;
    }

    public int getId() {
        return id;
    }
    public String getName(){
        return name;
    }
    public String getAbbreviation(){
        return abbreviation;
    }
    @Override
    public String toString() {
        return id + ": " + name + " (" + abbreviation + ")";
    }
}
