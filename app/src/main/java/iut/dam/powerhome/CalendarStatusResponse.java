package iut.dam.powerhome;

import com.google.gson.GsonBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
//equivlent de la reponse complete de getCalendarStatuts.php
//affiche le mois avec la liste des 14 j
public class CalendarStatusResponse {
    String month_label;
    String start_date;
    int nb_habitants;
    int capacite_creneau;
    List<CalendarDay> days;

    public String getMonth_label()     { return month_label; }
    public String getStart_date()      { return start_date; }
    public int getNb_habitants()       { return nb_habitants; }
    public int getCapacite_creneau()   { return capacite_creneau; }
    public List<CalendarDay> getDays() { return days; }

    //toujours pour la deserialization
    public static CalendarStatusResponse getFromJson(String json) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(CalendarDay.class, new CalendarDay.CalendarDayDeserializer())
                .create();
        return gson.fromJson(json, CalendarStatusResponse.class);
    }
}