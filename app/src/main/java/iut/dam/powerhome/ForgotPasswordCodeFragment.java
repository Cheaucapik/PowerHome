package iut.dam.powerhome;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

public class ForgotPasswordCodeFragment extends Fragment {
    private View layout;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.forgot_password_code_fragment, container, false);

        Button btnVerify = layout.findViewById(R.id.btn_verify);
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verify(v);
            }
        });

        Button btnSend = layout.findViewById(R.id.btn_sendAgain);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send(v);
            }
        });


        return layout;
    }

    public void send(View v){
        SharedPreferences sp = getContext().getSharedPreferences("CodeSession", MODE_PRIVATE);
        String json = sp.getString("code_json", null);

        if(json != null) {
            User currentUser = User.getFromJson(json);
            String id = (String) String.valueOf(currentUser.id);

            String url = "http://10.0.2.2/powerhome_server/send.php?id=" + id;

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

                            Log.d("DEBUG_SERVER", "Réponse du PHP : " + result);

                            if (result != null && result.contains("success")) {
                                try {
                                    JSONObject jo = new JSONObject(result);
                                    String code = jo.getString("code");
                                    Toast.makeText(getContext(), "Code renvoyé", Toast.LENGTH_SHORT).show();
                                    Log.i("Code", code);
                                } catch (JSONException ex) {
                                    Log.e("JSON_PARSE", "Erreur lors de la lecture du JSON : " + result);
                                }
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
    }

    public void verify(View v){
        SharedPreferences sp = getContext().getSharedPreferences("CodeSession", MODE_PRIVATE);
        String json = sp.getString("code_json", null);

        if(json != null){
            User currentUser = User.getFromJson(json);
            String id = (String) String.valueOf(currentUser.id);

            EditText code_et = layout.findViewById(R.id.code_et);
            String code = code_et.getText().toString().trim();

            String url = "http://10.0.2.2/powerhome_server/verify.php?id=" + id +"&code=" + code;

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

                            Log.d("DEBUG_SERVER", "Réponse du PHP : " + result);

                            if (result != null && result.contains("success")) {
                                ForgotPasswordActivity.verify();
                            }

                            else {
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
    }
}
