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

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isReady = true;
        }, 1000);
    }

    public void signup(View v) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void forgot(View v){
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
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
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e != null) {
                            Log.e("Erreur", "Problème réseau", e);
                            Toast.makeText(LoginActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Log.d("DEBUG_SERVER", "Réponse du PHP : " + result);

                        if (result != null && result.contains("token")) {
                            try {
                                JSONObject jo = new JSONObject(result);

                                SharedPreferences spFragment = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor edFrag = spFragment.edit();
                                edFrag.putString("token", jo.getString("token"));
                                edFrag.putString("firstname", jo.optString("firstname", ""));
                                edFrag.putString("lastname", jo.optString("lastname", ""));
                                edFrag.putString("email", jo.optString("email", ""));
                                edFrag.apply();

                                SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("user_json", result);
                                editor.apply();

                                boolean token_null = jo.optBoolean("token_null", false);
                                Intent intent;
                                if(token_null){
                                    intent = new Intent(LoginActivity.this, AddApplianceActivity.class);
                                } else {
                                    intent = new Intent(LoginActivity.this, MainActivity.class);
                                }
                                startActivity(intent);
                                finish();

                            } catch (JSONException ex) {
                                Log.e("JSON_PARSE", "Erreur : " + result);
                            }
                        } else {
                            try {
                                JSONObject jo = new JSONObject(result);
                                String error = jo.optString("error", "Identifiants incorrects");
                                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                            } catch (Exception ex) {
                                Toast.makeText(LoginActivity.this, "Erreur serveur", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}