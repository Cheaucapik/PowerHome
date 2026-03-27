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
            Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://10.0.2.2/powerhome_server/login.php";

        Ion.with(this)
                .load("POST", url)
                .setBodyParameter("id", id)
                .setBodyParameter("password", password)
                .asString()
                .setCallback((e, result) -> {

                    if (e != null) {
                        Log.e("LoginError", "Erreur réseau", e);
                        Toast.makeText(LoginActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d("DEBUG_SERVER", result);

                    try {
                        JSONObject jo = new JSONObject(result);

                        if (jo.has("token")) {
                            Toast.makeText(this, getString(R.string.welcome_back), Toast.LENGTH_SHORT).show();

                            SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor ed = sp.edit();

                            ed.putString("token", jo.getString("token"));
                            ed.putString("firstname", jo.optString("firstname", ""));
                            ed.putString("lastname", jo.optString("lastname", ""));
                            ed.putString("email", jo.optString("email", ""));
                            ed.putString("username", jo.optString("username", ""));
                            ed.apply();

                            SharedPreferences spSession = getSharedPreferences("UserSession", MODE_PRIVATE);
                            SharedPreferences.Editor edSession = spSession.edit();
                            edSession.putString("user_json", result);
                            edSession.apply();

                            boolean token_null = jo.optBoolean("token_null", false);

                            Intent intent = token_null ? new Intent(this, AddApplianceActivity.class) : new Intent(this, MainActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            String error = jo.optString("error", "Erreur");
                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException ex) {
                        Toast.makeText(this, getString(R.string.error_server), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}