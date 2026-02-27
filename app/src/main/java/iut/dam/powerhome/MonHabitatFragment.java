package iut.dam.powerhome;

import static android.content.Context.MODE_PRIVATE;

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

import java.util.ArrayList;
import java.util.List;

public class MonHabitatFragment extends Fragment {

    private List<Appliance> appliances = new ArrayList<>();

    public MonHabitatFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.monhabitat_fragment, container, false);
        SharedPreferences sp = getContext().getSharedPreferences("UserSession", MODE_PRIVATE);

        TextView name_tv = layout.findViewById(R.id.name_tv);
        String nom = sp.getString("firstname", "") + " " + sp.getString("lastname", "");
        name_tv.setText(nom);

        String token = sp.getString("token", "");

        String url = "http://10.0.2.2/powerhome_server/profil.php?token="+ token;

        Ion.with(this)
                .load(url)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e != null) {
                            Log.e("Erreur", "Problème réseau", e);
                            return;
                        }
                        if (result != null){
                            Log.d("Resultat", result);
//                            appliances.addAll(Appliance.getListFromJson(result));
                        } else {
                            Toast.makeText(getContext(), "Erreur : " + result, Toast.LENGTH_LONG).show();
                        }
                    }
                });

        return layout;
    }
}