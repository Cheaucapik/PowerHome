package iut.dam.powerhome;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.w3c.dom.Text;

import java.util.ArrayList;



public class MonHabitatFragment extends Fragment {

    private View layout;
    private ArrayList<Appliance> myAppliances = new ArrayList<>();
    private ApplianceAdapter adapter;

    public MonHabitatFragment() {}

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.monhabitat_fragment, container, false);
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
        else{
            Toast.makeText(getContext(), "Aucun utilisateur connecté", Toast.LENGTH_SHORT).show();
        }


        if(json!=null){
            User currentUser = User.getFromJson(json);
            String myEmail = currentUser.email;

            ListView lv = layout.findViewById(R.id.lv_appliances);

            String url = "http://10.0.2.2/powerhome_server/getHabitatByUser.php?email=" + myEmail;

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

                            Habitat habitatUser = Habitat.getFromJson(result);

                            if (habitatUser == null) {
                                Toast.makeText(getContext(), "Habitat introuvable pour " + myEmail, Toast.LENGTH_LONG).show();
                                return;
                            }

                            myAppliances = (ArrayList<Appliance>) habitatUser.getAppliances();
                            if(myAppliances.size() == 0){
                                TextView nodevice_tv = layout.findViewById(R.id.nodevice_tv);
                                nodevice_tv.setText(getContext().getString(R.string.you_don_t_have_any_appliance_yet));
                            }

                            adapter = new ApplianceAdapter(requireContext(), R.layout.item_info, myAppliances);
                            lv.setAdapter(adapter);

                        }
                    });
        }

        ImageView edit_iv = layout.findViewById(R.id.iv_edit);
        edit_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDialogEditAppliance cs = new CustomDialogEditAppliance(getContext(), myAppliances, adapter);
                cs.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        update();
                    }
                });
                cs.show();
            }
        });

        return layout;
    }

    public void update(){
        TextView conso_tv = layout.findViewById(R.id.conso_tv);
        TextView nb_appareil_tv = layout.findViewById(R.id.nb_appareil_tv);

        int sum = 0;
        for(Appliance a : myAppliances){
            sum += a.wattage;
        }
        conso_tv.setText(sum + " W");
        nb_appareil_tv.setText(myAppliances.size() + " " + getContext().getString(R.string.appliance));
        adapter.notifyDataSetChanged();
    }
}