package iut.dam.powerhome;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;

public class Appliance implements Serializable {
    int id;
    String name;
    String reference;
    int wattage;
    int d;

    public Appliance(int id, String name, String reference, int wattage, int d){
        this.id = id;
        this.name = name;
        this.reference = reference;
        this.wattage = wattage;
        this.d = d;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getReference() {
        return reference;
    }

    public int getWattage() {
        return wattage;
    }

    public int getD() {
        if (this.name == null) return R.drawable.default_icon;

        switch (this.name) {
            case "appliance_washing_machine":
                return R.drawable.washing_machine;
            case "appliance_vacuum_cleaner":
                return R.drawable.vacuum;
            case "appliance_air_conditionner":
                return R.drawable.air_conditioning;
            case "appliance_steam_ironer":
                return R.drawable.steam_iron;
            case "appliance_fridge":
                return R.drawable.fridge_svgrepo_com;
            case "appliance_pc":
                return R.drawable.computer_svgrepo_com;
            case "appliance_oven":
                return R.drawable.oven;
            case "appliance_tv":
                return R.drawable.tv;
            default:
                return R.drawable.default_icon;
        }
    }

    public static List<Appliance> getListFromJson(String json){
        Gson gson = new Gson();
        Type type = new TypeToken<List<Appliance>>(){}.getType();
        List<Appliance> list = gson.fromJson(json, type);
        return list;
    }
}
