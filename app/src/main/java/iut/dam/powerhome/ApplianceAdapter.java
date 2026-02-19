package iut.dam.powerhome;

import android.content.Context;
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
        name.setText(item.getName());
        ref.setText(item.getReference());
        watts.setText(item.getWattage() + "W");

        colorWattage(watts, item);

        return convertView;
    }

    private void colorWattage(TextView tv, Appliance item){
        tv.setTextColor(Color.WHITE);
        if(item.getWattage() < 1000){
            tv.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green));
        }
        else if(item.getWattage() < 2500){
            tv.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.orange));
        }
        else{
            tv.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
        }
    }

}
