package iut.dam.powerhome;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

public class CustomDialogAddAppliance extends Dialog {

    public CustomDialogAddAppliance(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_info_add_appliance);

        Spinner applianceSP = findViewById(R.id.appliance_sp);
        String[] items = {getContext().getString(R.string.appliance_steam_ironer),
                getContext().getString(R.string.appliance_air_conditionner),
                getContext().getString(R.string.appliance_washing_machine),
                getContext().getString(R.string.appliance_vacuum_cleaner)};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_list_item_1,
                        items);
        applianceSP.setAdapter(adapter);


        findViewById(R.id.btn_signup).setOnClickListener(view -> {
            String appliance = applianceSP.getSelectedItem().toString();

            EditText consumption_et = findViewById(R.id.consumption_et);
            String wattage = consumption_et.getText().toString().trim();

            EditText ref_et = findViewById(R.id.ref_et);
            String ref = ref_et.getText().toString().trim();

            if (wattage.isEmpty()) {
                consumption_et.setError("Indiquez la consommation");
                return;
            }

            if (ref.isEmpty()) {
                ref_et.setError("Indiquez une référence");
                return;
            }

            SharedPreferences sp = getContext().getSharedPreferences("UserSession", MODE_PRIVATE);
            String json = sp.getString("user_json", null);

            if(json != null){
                User currentUser = User.getFromJson(json);
                String idHab = currentUser.habitat.id + "";

                String url = "http://10.0.2.2/powerhome_server/addAppliance.php";

                Ion.with(getContext())
                        .load("POST", url) // On force la méthode POST
                        .setBodyParameter("idHab", idHab)
                        .setBodyParameter("ref", ref)
                        .setBodyParameter("wattage", wattage)
                        .setBodyParameter("appliance", appliance)
                        .asString()
                        .setCallback(new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result) {
                                if (e != null) {
                                    Log.e("Erreur", "Problème réseau", e);
                                    return;
                                }
                                if (result != null && result.contains("success")) {
                                    Toast.makeText(getContext(), "Appareil ajouté !", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent (getContext(), MainActivity.class);
                                    getContext().startActivity(intent);
                                    ((AddApplianceActivity) getContext()).finish();
                                } else {
                                    try {
                                        JSONObject jo = new JSONObject(result);
                                        String error = jo.getString("error");
                                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                                        Log.d("PB_ID", "Serveur : " + error);
                                    }
                                    catch (JSONException ex) {
                                        Log.e("JSON_PARSE", "Erreur lors de la lecture du JSON : " + result);
                                    }
                                }
                            }
                        });
            }
            else{
                Toast.makeText(getContext(), "erreur", Toast.LENGTH_LONG).show();
            }
        });

    }
}
