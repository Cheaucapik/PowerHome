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

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;

    // URL pour l'émulateur Android Studio (10.0.2.2 = localhost du PC)
    private static final String LOGIN_URL = "http://10.0.2.2/powerhome_server/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Chargement du layout
        setContentView(R.layout.loginactivity);

        // 2. Liaison avec le XML
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);

        // 3. Action du clic sur Sign In
        if (btnLogin != null) {
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performLogin();
                }
            });
        }
    }

    // Fonction déclenchée au clic
    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lancement de la requête réseau en tâche de fond
        new LoginTask().execute(email, password);
    }

    // Ces méthodes sont appelées si tu as android:onClick dans ton XML
    public void login(View view) {
        performLogin();
    }

    public void signup(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    // Classe pour gérer la connexion HTTP
    private class LoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String email = params[0];
            String password = params[1];
            String fullUrl = LOGIN_URL + "?email=" + email + "&password=" + password;

            try {
                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();

            } catch (Exception e) {
                Log.e("POWERHOME", "Erreur réseau : " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(LoginActivity.this, "Serveur XAMPP injoignable", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                JSONObject json = new JSONObject(result);

                if (json.has("token")) {
                    // Succès : On sauvegarde le token pour les prochaines activités
                    SharedPreferences pref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    pref.edit()
                            .putString("token", json.getString("token"))
                            .putInt("userId", json.getInt("id"))
                            .apply();

                    Toast.makeText(LoginActivity.this, "Bienvenue " + json.getString("firstname"), Toast.LENGTH_SHORT).show();

                    // Direction l'écran principal
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Erreur renvoyée par ton PHP (401)
                    String error = json.optString("error", "Identifiants incorrects");
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("POWERHOME", "Erreur JSON : " + e.getMessage());
                Toast.makeText(LoginActivity.this, "Erreur de réponse serveur", Toast.LENGTH_SHORT).show();
            }
        }
    }
}