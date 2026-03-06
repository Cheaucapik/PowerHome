package iut.dam.powerhome;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class ApplianceAdapter extends ArrayAdapter<Appliance> {
    private ArrayList<Appliance> applianceList;

    public ApplianceAdapter(@NonNull Context context, int resource, ArrayList<Appliance> applianceList) {
        super(context, resource, applianceList);
        this.applianceList = applianceList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Appliance item = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_info, parent, false);
        }

        ImageView icon = convertView.findViewById(R.id.appliance_icon_iv);
        TextView name = convertView.findViewById(R.id.appliance_name_tv);
        TextView ref = convertView.findViewById(R.id.appliance_ref_tv);
        TextView watts = convertView.findViewById(R.id.appliance_wattage_tv);

        icon.setImageResource(item.getD());

        // --- DEBUT DE LA LOGIQUE DE TRADUCTION ---
        String rawName = item.getName(); // ex: "appliance_washing_machine"

        // On cherche l'identifiant de la ressource string correspondant au nom
        int resId = getContext().getResources().getIdentifier(rawName, "string", getContext().getPackageName());

        if (resId != 0) {
            // Si la clé existe dans strings.xml, Android prend automatiquement la bonne langue
            name.setText(getContext().getString(resId));
        } else {
            // Si la clé n'est pas trouvée (vieux noms ou erreur), on affiche le texte brut
            name.setText(rawName);
        }
        // --- FIN DE LA LOGIQUE DE TRADUCTION ---

        ref.setText(item.getReference());
        watts.setText(item.getWattage() + "W");

        colorWattage(watts, item);

        return convertView;
    }

    private void colorWattage(TextView tv, Appliance item){
        tv.setTextColor(Color.WHITE);
        if(item.getWattage() < 1000){
            int color = ContextCompat.getColor(getContext(), R.color.green);
            tv.setBackgroundTintList(ColorStateList.valueOf(color));
        }
        else if(item.getWattage() < 2500){
            int color = ContextCompat.getColor(getContext(), R.color.orange);
            tv.setBackgroundTintList(ColorStateList.valueOf(color));
        }
        else{
            int color = ContextCompat.getColor(getContext(), R.color.red);
            tv.setBackgroundTintList(ColorStateList.valueOf(color));
        }
    }
}