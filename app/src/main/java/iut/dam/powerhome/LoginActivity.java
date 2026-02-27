package iut.dam.powerhome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class LoginActivity extends AppCompatActivity {
    private boolean isReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        final View content = findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        if (isReady) {
                            content.getViewTreeObserver().removeOnPreDrawListener(this);
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
        );

        new Handler(Looper.getMainLooper()).postDelayed(() -> isReady = true, 1000);

        EdgeToEdge.enable(this);
        setContentView(R.layout.loginactivity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginactivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void signup(View v) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void login(View v) {
        EditText editEmail = findViewById(R.id.et_email);
        EditText editPassword = findViewById(R.id.et_password);

        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // Validation email
        if (!isValidEmail(email)) {
            editEmail.setError("Email invalide (ex: email@domaine.com)");
            editEmail.requestFocus();
            return;
        }

        // Validation password
        if (!isValidPassword(password)) {
            editPassword.setError("Mot de passe: 1 minuscule, 1 majuscule, 1 spécial, min 8");
            editPassword.requestFocus();
            return;
        }

        String url = "http://10.0.2.2/powerhome_server/login.php?email="
                + encodeURIComponent(email)
                + "&password="
                + encodeURIComponent(password);

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

                        if (result != null && result.contains("token")) {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            try {
                                JSONObject json = new JSONObject(result);
                                String token = json.getString("token");
                                String firstname = json.getString("firstname");
                                String lastname = json.getString("lastname");
                                String email = json.getString("email");

                                editor.putString("token", token);
                                editor.putString("firstname", firstname);
                                editor.putString("lastname", lastname);
                                editor.putString("email", email);
                                editor.apply();

                                startActivity(intent);
                            } catch (JSONException ex) {
                                throw new RuntimeException(ex);
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Identifiants incorrects", Toast.LENGTH_SHORT).show();
                            Log.d("PB_ID", "Serveur : " + result);
                        }
                    }
                });
    }

    private boolean isValidEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$";
        return password != null && password.matches(regex);
    }

    private String encodeURIComponent(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }
}