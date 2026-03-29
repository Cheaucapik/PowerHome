package iut.dam.powerhome;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

public class ForgotPasswordMailFragment extends Fragment {

    private View layout;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.forgot_password_mail_fragment, container, false);

        Button btnRecover = layout.findViewById(R.id.btn_recover);
        btnRecover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recover(v);
            }
        });

        return layout;
    }

    private boolean isValidEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void recover(View v){
        EditText email_et = layout.findViewById(R.id.email_et);
        String email = email_et.getText().toString().trim();

        if (!isValidEmail(email)) {
            email_et.setError(getString(R.string.error_invalid_email));
            email_et.requestFocus();
            return;
        }

        String url = "http://10.0.2.2/www/recover.php?email=" + email;

        Ion.with(this)
                .load(url)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e != null) {
                            Log.e("API_RECOVER", "Erreur réseau", e);
                            return;
                        }

                        Log.d("DEBUG_SERVER", "Réponse du PHP : " + result);

                        if (result != null && result.contains("token")) {
                            SharedPreferences sp = getContext().getSharedPreferences("CodeSession", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("code_json", result);
                            editor.apply();

                            try {
                                JSONObject jo = new JSONObject(result);
                                boolean renvoie = jo.getBoolean("renvoyer_code");

                                if(renvoie){
                                    String code = jo.getString("code");
                                    Toast.makeText(getContext(), getString(R.string.success_code_sent), Toast.LENGTH_SHORT).show();
                                    Log.i("Code", code);
                                }
                                ForgotPasswordActivity.code();
                            } catch (JSONException ex) {
                                Log.e("JSON_PARSE", "Erreur JSON : " + result);
                            }
                        }
                        else {
                            try {
                                JSONObject jo = new JSONObject(result);
                                String error = jo.getString("error");
                                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                                Log.d("PB_ID", "Erreur serveur : " + error);
                            }
                            catch (JSONException ex) {
                                Log.e("JSON_PARSE", "Erreur JSON : " + result);
                            }
                        }
                    }
                });
    }
}