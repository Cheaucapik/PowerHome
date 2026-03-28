package iut.dam.powerhome;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CalendarDay {
    String date;
    double score;
    String color;
    boolean blocked;
    List<Timeslot> slots;
    //Liste avec les 3 créneaux du jour enft

    // on a utilisé tout les nv import de Gson, json aussi
    //json array (pour le tableau de creneaux (slot)

    public String getDate() { return date; }
    public double getScore() { return score; }
    public String getColor() { return color; }
    public boolean isBlocked() { return blocked; }
    public List<Timeslot> getSlots() { return slots; }

    //des getter standards quoi depuis le frag->acceder aux donnees

    // -------------------------------------------------------
    // Custom deserializer: maps "timeslot_id" → Timeslot.id
    // -------------------------------------------------------
    /**En gros c une sorte de convertisseurs ->puisque le php renvoie un .id
     * Désérialiseur Gson custom — nécessaire car le PHP renvoie timeslot_id mais le modèle java il va attendre un id.
     * paske sans, l'ID du timeslot serait toujours 0 et la réservation enverrait un mauvais timeslot_id au serveur
      sah a mieux comprendre stp
     **/
    public static class CalendarDayDeserializer implements JsonDeserializer<CalendarDay> {
        @Override
        public CalendarDay deserialize(JsonElement json, Type typeOfT,
                                       JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            CalendarDay day = new CalendarDay();

            day.date    = obj.has("date")    ? obj.get("date").getAsString()    : null;
            day.score   = obj.has("score")   ? obj.get("score").getAsDouble()   : 0;
            day.color   = obj.has("color")   ? obj.get("color").getAsString()   : null;
            day.blocked = obj.has("blocked") && obj.get("blocked").getAsBoolean();

            // Parse slots array, remapping "timeslot_id" → "id"
            if (obj.has("slots") && obj.get("slots").isJsonArray()) {
                JsonArray slotsArr = obj.getAsJsonArray("slots");
                day.slots = new ArrayList<>();
                for (JsonElement elem : slotsArr) {
                    JsonObject slotObj = elem.getAsJsonObject();
                    Timeslot t = new Timeslot();

                    // PHP sends "timeslot_id", our model needs "id"
                    if (slotObj.has("timeslot_id")) {
                        t.id = slotObj.get("timeslot_id").getAsInt();
                    } else if (slotObj.has("id")) {
                        t.id = slotObj.get("id").getAsInt();
                    }

                    t.label      = slotObj.has("label")      ? slotObj.get("label").getAsString()      : "";
                    t.start_hour = slotObj.has("start_hour") ? slotObj.get("start_hour").getAsInt()    : 0;
                    t.end_hour   = slotObj.has("end_hour")   ? slotObj.get("end_hour").getAsInt()      : 0;
                    t.slot_order = slotObj.has("slot_order") ? slotObj.get("slot_order").getAsInt()    : 0;
                    t.conso      = slotObj.has("conso")      ? slotObj.get("conso").getAsInt()         : 0;
                    t.score      = slotObj.has("score")      ? slotObj.get("score").getAsDouble()      : 0;
                    t.color      = slotObj.has("color")      ? slotObj.get("color").getAsString()      : null;
                    t.blocked    = slotObj.has("blocked")    && slotObj.get("blocked").getAsBoolean();

                    day.slots.add(t);
                }
            }

            return day;
        }
    }

    // -------------------------------------------------------
    // Gson instance with the custom deserializer registered
    // ducoup il va construire une instance Gson AVEC le deserialiseur saved
    // -------------------------------------------------------
    private static Gson buildGson() {
        return new GsonBuilder()
                .registerTypeAdapter(CalendarDay.class, new CalendarDayDeserializer())
                .create();
    }
//La cest juste les points d'entree pour truquer les jason recus
    public static CalendarDay getFromJson(String json) {
        return buildGson().fromJson(json, CalendarDay.class);
    }

    public static List<CalendarDay> getListFromJson(String json) {
        Type type = new TypeToken<List<CalendarDay>>() {}.getType();
        return buildGson().fromJson(json, type);
    }
}