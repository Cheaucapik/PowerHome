package iut.dam.powerhome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LoginActivity extends AppCompatActivity {

    private EditText etId, etPassword;
    private Button btnLogin;
    // Utilise bien 10.0.2.2 pour l'émulateur standard
    private static final String LOGIN_URL = "http://10.0.2.2/powerhome_server/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginactivity);

        etId = findViewById(R.id.et_id);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> performLogin());
        }
    }

    private void performLogin() {
        String idInput = etId.getText().toString().trim();
        String passwordInput = etPassword.getText().toString().trim();

        if (idInput.isEmpty() || passwordInput.isEmpty()) {
            Toast.makeText(this, "Champs vides", Toast.LENGTH_SHORT).show();
            return;
        }
        new LoginTask().execute(idInput, passwordInput);
    }

    private class LoginTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                String email = URLEncoder.encode(params[0], "UTF-8");
                String password = URLEncoder.encode(params[1], "UTF-8");
                String fullUrl = LOGIN_URL + "?email=" + email + "&password=" + password;

                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(7000); // 7 secondes pour laisser le temps au réseau

                // On récupère le code de réponse (200, 401, 400, etc.)
                int responseCode = conn.getResponseCode();

                BufferedReader reader;
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();

            } catch (Exception e) {
                Log.e("POWERHOME", "Erreur réseau : " + e.getMessage());
                return "ERREUR_RESEAU";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("ERREUR_RESEAU")) {
                Toast.makeText(LoginActivity.this, "Problème de connexion au PC (10.0.2.2)", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                JSONObject json = new JSONObject(result);
                if (json.has("token")) {
                    SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("token", json.getString("token"));
                    editor.putInt("userId", json.getInt("id"));
                    editor.putString("firstname", json.getString("firstname"));
                    editor.putString("lastname", json.getString("lastname"));
                    editor.putString("email", json.getString("email"));
                    editor.apply();

                    Toast.makeText(LoginActivity.this, "Succès !", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, json.optString("error"), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(LoginActivity.this, "Erreur JSON : " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}