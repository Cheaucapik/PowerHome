package iut.dam.powerhome;

import com.google.gson.Gson;

public class Timeslot {
    // UN creneau horaire (une des 3 ducoup) avec son etat daffluence actuel1
    int id;
    String label;
    int start_hour;
    int end_hour;
    int slot_order;
    //montrer que 7-12 passe avant le creneau dapres
    //important car cest ce que va utiliser requestfrag (classe)
    //pour associer chaque timeslot au bon bouton de creneau (tvslotmorning...)

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