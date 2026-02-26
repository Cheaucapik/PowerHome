package iut.dam.powerhome;

public class Appliance {
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
            case "Machine à laver":
                return R.drawable.washing_machine;
            case "Aspirateur":
                return R.drawable.vacuum;
            case "Climatiseur":
                return R.drawable.air_conditioning;
            case "Repasseur vapeur":
                return R.drawable.steam_iron;
            default:
                return R.drawable.default_icon;
        }
    }
}
