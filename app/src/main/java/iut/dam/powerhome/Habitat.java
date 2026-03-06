package iut.dam.powerhome;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Habitat {
    int id;
    int floor;
    double area;
    List<Appliance> appliances;
    String residentName;
    public Habitat(int id, int floor, double area, ArrayList<Appliance> appliances, String residentName){
        this.id = id;
        this.floor = floor;
        this.area = area;
        this.appliances = appliances;
        this.residentName = residentName;
    }

    public String getResidentName(){
        return residentName;
    }

    public int getId() {
        return id;
    }

    public int getFloor() {
        return floor;
    }

    public double getArea() {
        return area;
    }

    public List<Appliance> getAppliances() {
        return appliances;
    }

    public static Habitat getFromJson(String json){
        Gson gson = new Gson();
        Habitat obj = gson.fromJson(json, Habitat.class);
        return obj;
    }

    public static List<Habitat> getListFromJson(String json){
        Gson gson = new Gson();
        Type type = new TypeToken<List<Habitat>>(){}.getType();
        List<Habitat> list = gson.fromJson(json, type);
        return list;
    }
}