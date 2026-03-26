package iut.dam.powerhome;

import com.google.gson.Gson;

public class Timeslot {
    int id;
    String label;
    int start_hour;
    int end_hour;
    int slot_order;

    int conso;
    double score;
    String color;
    boolean blocked;

    public Timeslot() {
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public int getStart_hour() {
        return start_hour;
    }

    public int getEnd_hour() {
        return end_hour;
    }

    public int getSlot_order() {
        return slot_order;
    }

    public int getConso() {
        return conso;
    }

    public double getScore() {
        return score;
    }

    public String getColor() {
        return color;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public static Timeslot getFromJson(String json) {
        return new Gson().fromJson(json, Timeslot.class);
    }
}