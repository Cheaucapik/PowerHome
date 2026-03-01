package iut.dam.powerhome;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MonHabitatFragment extends Fragment {

    private List<Appliance> appliances = new ArrayList<>();

    public MonHabitatFragment(){

    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.monhabitat_fragment, container, false);
        SharedPreferences sp = getContext().getSharedPreferences("UserSession", MODE_PRIVATE);

        TextView name_tv = layout.findViewById(R.id.name_tv);
        TextView nb_appareil_tv = layout.findViewById(R.id.nb_appareil_tv);
        TextView pseudo_tv = layout.findViewById(R.id.pseudo_tv);
        TextView etage_tv = layout.findViewById(R.id.etage_tv);
        TextView surface_tv = layout.findViewById(R.id.surface_tv);
        TextView conso_tv = layout.findViewById(R.id.conso_tv);


        String json = sp.getString("user_json", null);

        if(json != null){
            User currentUser = User.getFromJson(json);
            name_tv.setText(currentUser.firstname + " " + currentUser.lastname);
            nb_appareil_tv.setText(currentUser.habitat.getAppliances().size() + " " + getContext().getString(R.string.appliance) + (currentUser.habitat.getAppliances().size() > 1 ? "s" : ""));
            pseudo_tv.setText(currentUser.username);
            etage_tv.setText(currentUser.habitat.floor + "");
            surface_tv.setText(currentUser.habitat.area + " m²");
            int sum = 0;
            for(Appliance a : currentUser.habitat.appliances){
                sum += a.wattage;
            }
            conso_tv.setText(sum + " W");
        }

        return layout;
    }
}