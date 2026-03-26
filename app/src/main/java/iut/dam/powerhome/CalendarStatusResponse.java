package iut.dam.powerhome;

import com.google.gson.Gson;

import java.util.List;

public class CalendarStatusResponse {
    String month_label;
    String start_date;
    int nb_habitants;
    int capacite_creneau;
    List<CalendarDay> days;

    public String getMonth_label() {
        return month_label;
    }

    public String getStart_date() {
        return start_date;
    }

    public int getNb_habitants() {
        return nb_habitants;
    }

    public int getCapacite_creneau() {
        return capacite_creneau;
    }

    public List<CalendarDay> getDays() {
        return days;
    }

    public static CalendarStatusResponse getFromJson(String json) {
        return new Gson().fromJson(json, CalendarStatusResponse.class);
    }
}