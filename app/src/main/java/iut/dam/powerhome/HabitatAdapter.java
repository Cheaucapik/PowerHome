package iut.dam.powerhome;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class HabitatAdapter extends ArrayAdapter<Habitat> {
    private ArrayList<Habitat> habitatList;

    public HabitatAdapter(@NonNull Context context, int resource, ArrayList<Habitat> habitatList) {
        super(context, resource, habitatList);
        this.habitatList = habitatList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Habitat habitat = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_habitat, parent, false);
        }

        TextView nomTv = convertView.findViewById(R.id.nom_habitant_tv);
        TextView etageTv = convertView.findViewById(R.id.etage_tv);
        TextView nbEqTv = convertView.findViewById(R.id.nb_equipements_tv);

        nomTv.setText(habitat.getResidentName());
        etageTv.setText(String.valueOf(habitat.getFloor()));

        String equipementText = habitat.getAppliances().size() + " équipement" + (habitat.getAppliances().size() > 1 ? "s" : "");
        nbEqTv.setText(equipementText);

        LinearLayout layout = convertView.findViewById(R.id.container_icons_LL);

        ArrayList<Appliance> appliance = (ArrayList<Appliance>) habitat.getAppliances();

        for(Appliance a : appliance){
            ImageView image = new ImageView(getContext());
            image.setImageResource(a.getD());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(10, 0, 0, 0);
            image.setLayoutParams(params);

            layout.addView(image);
        }

        return convertView;
    }

}
