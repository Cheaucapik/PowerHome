package iut.dam.powerhome;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
// Represente reservation retournee par getReservationByUser
//possede timeslot et la liste des appliances reserve
public class Reservation {
    int id;
    String reservation_date;
    int eco_coin_delta;
    Timeslot timeslot;
    List<Appliance> appliances;

    public Reservation() {
    }

    public int getId() {
        return id;
    }

    public String getReservation_date() {
        return reservation_date;
    }

    public int getEco_coin_delta() {
        return eco_coin_delta;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public List<Appliance> getAppliances() {
        return appliances;
    }

    public static Reservation getFromJson(String json) {
        return new Gson().fromJson(json, Reservation.class);
    }

    public static List<Reservation> getListFromJson(String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<Reservation>>() {}.getType();
        return gson.fromJson(json, type);
    }
}