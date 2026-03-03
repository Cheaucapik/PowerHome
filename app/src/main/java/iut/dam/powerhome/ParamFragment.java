package iut.dam.powerhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ParamFragment extends Fragment {

    private EditText etFname, etLname, etEmail, etPass;
    private String userToken;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Vérifie que le nom du layout est bien parametres_fragment.xml
        View layout = inflater.inflate(R.layout.parametres_fragment, container, false);

        etFname = layout.findViewById(R.id.et_firstname);
        etLname = layout.findViewById(R.id.et_lastname);
        etEmail = layout.findViewById(R.id.et_email);
        etPass = layout.findViewById(R.id.et_password);

        // Correction : On utilise "UserPrefs" (le nom utilisé dans ton LoginActivity)
        SharedPreferences sp = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userToken = sp.getString("token", "");

        // Remplissage auto des champs avec les infos actuelles
        etFname.setText(sp.getString("firstname", ""));
        etLname.setText(sp.getString("lastname", ""));
        etEmail.setText(sp.getString("email", ""));

        layout.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String fname = etFname.getText().toString().trim();
            String lname = etLname.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass  = etPass.getText().toString().trim();

            new UpdateUserTask().execute(userToken, fname, lname, email, pass);
        });

        return layout;
    }

    private class UpdateUserTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String token = params[0];
            String fname = params[1];
            String lname = params[2];
            String email = params[3];
            String pass  = params[4];

            try {
                // On utilise POST pour envoyer des données sensibles comme le mot de passe
                URL url = new URL("http://10.0.2.2/powerhome_server/update_user.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // Construction des données à envoyer
                String postData = "token=" + URLEncoder.encode(token, "UTF-8") +
                        "&firstname=" + URLEncoder.encode(fname, "UTF-8") +
                        "&lastname=" + URLEncoder.encode(lname, "UTF-8") +
                        "&email=" + URLEncoder.encode(email, "UTF-8") +
                        "&password=" + URLEncoder.encode(pass, "UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                return sb.toString();

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(getContext(), "Erreur serveur", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject json = new JSONObject(result);
                if (json.optString("status").equals("success")) {
                    // Mise à jour locale des SharedPreferences
                    SharedPreferences sp = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                    sp.edit()
                            .putString("firstname", etFname.getText().toString())
                            .putString("lastname", etLname.getText().toString())
                            .putString("email", etEmail.getText().toString())
                            .apply();

                    Toast.makeText(getContext(), "Profil mis à jour !", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Erreur : " + json.optString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Erreur de réponse", Toast.LENGTH_SHORT).show();
            }
        }
    }
}