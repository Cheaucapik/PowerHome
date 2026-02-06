package iut.dam.powerhome;
import java.util.List;

public class Habitat {
    int id;
    String residentName;
    int floor;
    double area;
    List<Appliance> appliances;
    public Habitat(int id){
        this.id = id;
    }
}
