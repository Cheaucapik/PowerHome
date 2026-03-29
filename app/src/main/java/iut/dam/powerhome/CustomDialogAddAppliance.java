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

    private final String[] rawItems = {
            "appliance_steam_ironer",
            "appliance_air_conditionner",
            "appliance_washing_machine",
            "appliance_vacuum_cleaner"
    };

    public CustomDialogAddAppliance(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_info_add_appliance);

        Spinner applianceSP = findViewById(R.id.appliance_sp);

        String[] displayItems = {
                getContext().getString(R.string.appliance_steam_ironer),
                getContext().getString(R.string.appliance_air_conditionner),
                getContext().getString(R.string.appliance_washing_machine),
                getContext().getString(R.string.appliance_vacuum_cleaner)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1,
                displayItems);
        applianceSP.setAdapter(adapter);

        findViewById(R.id.btn_signup).setOnClickListener(view -> {
            int selectedPosition = applianceSP.getSelectedItemPosition();
            String applianceRawName = rawItems[selectedPosition];

            EditText consumption_et = findViewById(R.id.consumption_et);
            String wattage = consumption_et.getText().toString().trim();

            EditText ref_et = findViewById(R.id.ref_et);
            String ref = ref_et.getText().toString().trim();

            if (wattage.isEmpty()) {
                consumption_et.setError(getContext().getString(R.string.error_missing_wattage));
                return;
            }

            if (ref.isEmpty()) {
                ref_et.setError(getContext().getString(R.string.error_missing_ref));
                return;
            }

            SharedPreferences sp = getContext().getSharedPreferences("UserSession", MODE_PRIVATE);
            String json = sp.getString("user_json", null);

            if(json != null){
                User currentUser = User.getFromJson(json);
                String idHab = String.valueOf(currentUser.habitat.id);

                String url = "http://10.0.2.2/powerhome_server/addAppliance.php";

                Ion.with(getContext())
                        .load("POST", url)
                        .setBodyParameter("idHab", idHab)
                        .setBodyParameter("ref", ref)
                        .setBodyParameter("wattage", wattage)
                        .setBodyParameter("appliance", applianceRawName)
                        .asString()
                        .setCallback(new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result) {
                                if (e != null) {
                                    Log.e("AddAppliance", "Erreur réseau", e);
                                    return;
                                }
                                if (result != null && result.contains("success")) {
                                    Toast.makeText(getContext(), getContext().getString(R.string.success_appliance_added), Toast.LENGTH_SHORT).show();

                                    if (getContext() instanceof AddApplianceActivity) {
                                        Intent intent = new Intent(getContext(), MainActivity.class);
                                        getContext().startActivity(intent);
                                        ((AddApplianceActivity) getContext()).finish();
                                    }
                                    dismiss();
                                } else {
                                    try {
                                        JSONObject jo = new JSONObject(result);
                                        String error = jo.optString("error", "Erreur inconnue");
                                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                                    } catch (JSONException ex) {
                                        Log.e("JSON_PARSE", "Erreur JSON: " + result);
                                    }
                                }
                            }
                        });
            } else {
                Toast.makeText(getContext(), getContext().getString(R.string.error_generic), Toast.LENGTH_LONG).show();
            }
        });
    }
}