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

    public void done(View v){
        SharedPreferences sp = getContext().getSharedPreferences("CodeSession", MODE_PRIVATE);
        String json = sp.getString("code_json", null);

        if(json != null) {
            User currentUser = User.getFromJson(json);
            String id = (String) String.valueOf(currentUser.id);

            EditText password_et = layout.findViewById(R.id.password_et);
            String password = password_et.getText().toString().trim();

            if(!isValidPassword(password)){
                password_et.setError("Mot de passe: 1 minuscule, 1 majuscule, 1 spécial, min 8");
                password_et.requestFocus();
                return;
            }

            String url = "http://10.0.2.2/powerhome_server/done.php?id=" + id + "&password=" + password;

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
                                if (getActivity() instanceof ForgotPasswordActivity) {
                                    ((ForgotPasswordActivity) getActivity()).done();
                                }
                            } else {
                                Toast.makeText(getContext(), "Erreur : " + result, Toast.LENGTH_LONG).show();
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
