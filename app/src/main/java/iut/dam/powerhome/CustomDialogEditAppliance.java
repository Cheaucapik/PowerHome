package iut.dam.powerhome;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.IonContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CustomDialogEditAppliance extends Dialog {
    private ArrayList<Appliance> appliances;
    private ApplianceAdapter fragmentAdapter;
    private ApplianceAdapter adapter;

    public CustomDialogEditAppliance(Context context, ArrayList<Appliance> appliances, ApplianceAdapter fragmentAdapter) {
        super(context);
        this.appliances = appliances;
        this.fragmentAdapter = fragmentAdapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit_appliance);

        findViewById(R.id.add_tv).setOnClickListener(v -> {
            CustomDialogAddAppliance custom = new CustomDialogAddAppliance(getContext());
            custom.show();
        });

        ListView lv_appliances = findViewById(R.id.lv_appliances);

        adapter = new ApplianceAdapter(getContext(), R.layout.item_info, appliances);
        lv_appliances.setAdapter(adapter);

        TextView nodevice_tv = findViewById(R.id.nodevice_tv);
        lv_appliances.setEmptyView(nodevice_tv);


        lv_appliances.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Appliance a = appliances.get(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Supprimer")
                        .setMessage("Voulez-vous supprimer " + a.name + " ?")
                        .setPositiveButton("Supprimer", (dialog, which) -> {
                            int appliance_id = a.getId();
                            remove(appliance_id, position);
                        })
                        .setNegativeButton("Annuler", (dialog, which) -> {
                            dialog.dismiss();
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    public void remove(int id, int position) {
        String url = "http://10.0.2.2/powerhome_server/removeAppliance.php?id=" + id;

        Ion.with(getContext())
                .load(url)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e != null) {
                            Log.e("Erreur", "Problème réseau", e);
                            return;
                        }

                        if (result != null && result.contains("success")) {
                            appliances.remove(position);
                            adapter.notifyDataSetChanged();
                            fragmentAdapter.notifyDataSetChanged();

                            Toast.makeText(getContext(), "Appareil supprimé !", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                JSONObject jo = new JSONObject(result);
                                String error = jo.getString("error");
                                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                                Log.d("PB_ID", "Serveur : " + error);
                            } catch (JSONException ex) {
                                Log.e("JSON_PARSE", "Erreur lors de la lecture du JSON : " + result);
                            }
                        }
                    }
                });
    }
}
