package iut.dam.powerhome;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class CalendarDay {
    String date;
    double score;
    String color;
    List<Timeslot> slots;

    public CalendarDay() {
    }

    public String getDate() {
        return date;
    }

    public double getScore() {
        return score;
    }

    public String getColor() {
        return color;
    }

    public List<Timeslot> getSlots() {
        return slots;
    }

    public static CalendarDay getFromJson(String json) {
        return new Gson().fromJson(json, CalendarDay.class);
    }

    public static List<CalendarDay> getListFromJson(String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<CalendarDay>>() {}.getType();
        return gson.fromJson(json, type);
    }
}