package iut.dam.powerhome;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Habitat {
    int id;
    String residentName;
    int floor;
    double area;
    List<Appliance> appliances;
public Habitat(int id, String residentName, int floor, double area, ArrayList<Appliance> appliances){
        this.id = id;
        this.residentName = residentName;
        this.floor = floor;
        this.area = area;
        this.appliances = appliances;
    }

    public int getId() {
        return id;
    }

    public String getResidentName() {
        return residentName;
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
}
