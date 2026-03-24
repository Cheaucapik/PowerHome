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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private boolean isReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        setContentView(R.layout.loginactivity);

        final View content = findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnPreDrawListener(
                () -> {
                    if (isReady) return true;
                    return false;
                }
        );

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isReady = true;
        }, 1000);
    }

    public void signup(View v) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void forgot(View v){
        startActivity(new Intent(this, ForgotPasswordActivity.class));
    }

    public void login(View v) {
        EditText editId = findViewById(R.id.et_id);
        EditText editPassword = findViewById(R.id.et_password);

        String id = editId.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (id.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Champs vides", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://10.0.2.2/powerhome_server/login.php?id=" + id + "&password=" + password;

        Ion.with(this)
                .load(url)
                .asString()
                .setCallback((e, result) -> {

                    if (e != null) {
                        Toast.makeText(LoginActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d("DEBUG_SERVER", result);

                    try {
                        JSONObject jo = new JSONObject(result);

                        if (jo.has("token")) {

                            SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor ed = sp.edit();

                            String username = jo.optString("username", "");
                            if ("null".equals(username)) username = "";

                            String firstname = jo.optString("firstname", "");
                            if ("null".equals(firstname)) firstname = "";

                            String lastname = jo.optString("lastname", "");
                            if ("null".equals(lastname)) lastname = "";

                            String email = jo.optString("email", "");
                            if ("null".equals(email)) email = "";

                            String tel = jo.optString("tel", "");
                            if ("null".equals(tel)) tel = "";

                            ed.putString("token", jo.getString("token"));
                            ed.putString("firstname", firstname);
                            ed.putString("lastname", lastname);
                            ed.putString("email", email);
                            ed.putString("username", username);
                            ed.putString("tel", tel);

                            ed.apply();

                            SharedPreferences spSession = getSharedPreferences("UserSession", MODE_PRIVATE);
                            SharedPreferences.Editor edSession = spSession.edit();
                            edSession.putString("user_json", result);
                            edSession.apply();

                            boolean token_null = jo.optBoolean("token_null", false);

                            Intent intent;
                            if (token_null) {
                                intent = new Intent(this, AddApplianceActivity.class);
                            } else {
                                intent = new Intent(this, MainActivity.class);
                            }

                            startActivity(intent);
                            finish();

                        } else {
                            String error = jo.optString("error", "Erreur");
                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException ex) {
                        Toast.makeText(this, "Erreur serveur", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}