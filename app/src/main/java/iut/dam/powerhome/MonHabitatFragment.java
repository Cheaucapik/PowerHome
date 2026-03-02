package iut.dam.powerhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MonHabitatFragment extends Fragment {

    private final ArrayList<Appliance> myAppliances = new ArrayList<>();
    private ApplianceAdapter adapter;

    public MonHabitatFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.monhabitat_fragment, container, false);

        // Vues du profil, on peut avoir nos xml comme as
        TextView nameTv = root.findViewById(R.id.name_tv);
        TextView etageTv = root.findViewById(R.id.etage_tv);
        TextView nbAppareilTv = root.findViewById(R.id.nb_appareil_tv);
        TextView surfaceTv = root.findViewById(R.id.surface_tv);
        TextView consoTv = root.findViewById(R.id.conso_tv);

        // ListView appareils
        ListView lv = root.findViewById(R.id.lv_appliances);
        adapter = new ApplianceAdapter(requireContext(), R.layout.item_info, myAppliances);
        lv.setAdapter(adapter);

        // Email du user connecté
        SharedPreferences sp = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String myEmail = sp.getString("email", null);
        if (myEmail == null) {
            Toast.makeText(getContext(), "Aucun utilisateur connecté", Toast.LENGTH_SHORT).show();
            return root;
        }

        String url = "http://10.0.2.2/powerhome_server/getHabitats.php";

        Ion.with(this)
                .load(url)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e != null) {
                            Log.e("MonHabitat", "Problème réseau", e);
                            Toast.makeText(getContext(), "Problème réseau", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (result == null) {
                            Toast.makeText(getContext(), "Réponse serveur vide", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            JSONArray habitats = new JSONArray(result);

                            JSONObject myHab = null;

                            for (int i = 0; i < habitats.length(); i++) {
                                JSONObject h = habitats.getJSONObject(i);
                                JSONObject resident = h.optJSONObject("resident");
                                String email = resident != null ? resident.optString("email", null) : null;

                                if (email != null && email.equalsIgnoreCase(myEmail)) {
                                    myHab = h;
                                    break;
                                }
                            }

                            if (myHab == null) {
                                Toast.makeText(getContext(), "Habitat introuvable pour " + myEmail, Toast.LENGTH_LONG).show();
                                return;
                            }

                            // Nous donne les infos de l'habitat
                            JSONObject resident = myHab.getJSONObject("resident");
                            String firstname = resident.optString("firstname", "");
                            String lastname = resident.optString("lastname", "");
                            int floor = myHab.optInt("floor", 0);
                            double area = myHab.optDouble("area", 0.0);

                            if (nameTv != null) nameTv.setText((firstname + " " + lastname).trim());
                            if (etageTv != null) etageTv.setText(String.valueOf(floor));
                            if (surfaceTv != null) surfaceTv.setText(area + " m²");

                            // Appareils
                            JSONArray apps = myHab.optJSONArray("appliances");

                            myAppliances.clear();
                            int totalW = 0;

                            if (apps != null) {
                                for (int j = 0; j < apps.length(); j++) {
                                    JSONObject a = apps.getJSONObject(j);
                                    int id = a.optInt("id", 0);
                                    String name = a.optString("name", "");
                                    String reference = a.optString("reference", "");
                                    int wattage = a.optInt("wattage", 0);

                                    myAppliances.add(new Appliance(id, name, reference, wattage, 0));
                                    totalW += wattage;
                                }
                            }

                            adapter.notifyDataSetChanged();

                            int n = myAppliances.size();
                            if (nbAppareilTv != null) nbAppareilTv.setText(n + " appareil" + (n > 1 ? "s" : ""));
                            if (consoTv != null) consoTv.setText(totalW + " W");

                        } catch (Exception ex) {
                            Log.e("MonHabitat", "Parsing JSON error", ex);
                            Toast.makeText(getContext(), "Erreur parsing JSON", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        return root;
    }
}