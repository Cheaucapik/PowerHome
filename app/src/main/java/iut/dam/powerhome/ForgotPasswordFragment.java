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

public class ForgotPasswordFragment extends Fragment {

    private View layout;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.forgot_password_fragment, container, false);

        Button btnDone = layout.findViewById(R.id.btn_done);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                done(v);
            }
        });

        return layout;
    }

    public void done(View v) {
        if (getContext() == null) return;
        SharedPreferences sp = getContext().getSharedPreferences("CodeSession", MODE_PRIVATE);
        String json = sp.getString("code_json", null);

        if (json != null) {
            User currentUser = User.getFromJson(json);
            String id = String.valueOf(currentUser.id);

            EditText password_et = layout.findViewById(R.id.password_et);
            String password = password_et.getText().toString().trim();

            if (!isValidPassword(password)) {
                password_et.setError(getString(R.string.error_password_regex));
                password_et.requestFocus();
                return;
            }

            String url = "http://10.0.2.2/www/done.php";

            Ion.with(this)
                    .load("POST", url)
                    .setBodyParameter("id", id)
                    .setBodyParameter("password", password)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            if (e != null) {
                                Log.e("API_DONE", "Problème réseau lors de la validation du mot de passe", e);
                                return;
                            }

                            Log.d("DEBUG_SERVER", "Réponse du PHP : " + result);

                            try {
                                JSONObject jo = new JSONObject(result);
                                if (jo.optString("status").equals("success")) {
                                    if (getActivity() instanceof ForgotPasswordActivity) {
                                        ((ForgotPasswordActivity) getActivity()).done();
                                    }
                                } else {
                                    String error = jo.optString("message", "Erreur inconnue");
                                    Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException ex) {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), getString(R.string.error_json), Toast.LENGTH_SHORT).show();
                                }
                                Log.e("JSON_PARSE", "Erreur JSON : " + result);
                            }
                        }
                    });
        }
    }

    private boolean isValidPassword(String password) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$";
        return password != null && password.matches(regex);
    }
}