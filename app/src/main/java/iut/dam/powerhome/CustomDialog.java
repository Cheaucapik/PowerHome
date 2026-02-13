package iut.dam.powerhome;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomDialog extends Dialog {
    private Habitat habitat;
    public CustomDialog(Context context, Habitat habitat){
        super(context);
        this.habitat = habitat;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_info);

        TextView nom = findViewById(R.id.nom_habitant_tv);
        nom.setText(habitat.getResidentName());

        TextView area = findViewById(R.id.area_tv);
        area.setText(habitat.getArea() + "m²");


        ListView appliancesLV = findViewById(R.id.appliance_lv);
        ArrayList<Appliance> appliances = (ArrayList<Appliance>) habitat.getAppliances();

        ApplianceAdapter adapter = new ApplianceAdapter(getContext(), R.layout.item_habitat, appliances);
        appliancesLV.setAdapter(adapter);
    }
}
