package iut.dam.powerhome;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.registeractivity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Spinner prefixeSP = findViewById(R.id.sp_prefixe);
        String[] items = {"+1", "+33", "+34"};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1,
                        items);
        prefixeSP.setAdapter(adapter);
    }

    public void signin(View v) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void sign(View v) {
        CustomDialogSignUp customDialogSignUp = new CustomDialogSignUp(this);
        customDialogSignUp.show();
    }


    public void sendDataToServer(String floor, String area) {
        String email = ((EditText) findViewById(R.id.email_et)).getText().toString().trim();
        String password = ((EditText) findViewById(R.id.password_et)).getText().toString().trim();

        if (!isValidEmail(email)) {
            EditText emailEt = findViewById(R.id.email_et);
            emailEt.setError(getString(R.string.error_invalid_email));
            emailEt.requestFocus();
            return;
        }

        if (!isValidPassword(password)) {
            EditText passwordEt = findViewById(R.id.password_et);
            passwordEt.setError(getString(R.string.error_password_regex));
            passwordEt.requestFocus();
            return;
        }

        String lastname = ((EditText) findViewById(R.id.lastname_et)).getText().toString().trim();
        String firstname = ((EditText) findViewById(R.id.firstname_et)).getText().toString().trim();
        String username = ((EditText) findViewById(R.id.username_et)).getText().toString().trim();

        Spinner prefixe_sp = findViewById(R.id.sp_prefixe);
        String tel_brut = ((EditText) findViewById(R.id.tel_et)).getText().toString().trim();
        String tel = prefixe_sp.getSelectedItem().toString() + "-" + tel_brut;

        String url = "http://10.0.2.2/powerhome_server/signup.php";


        Ion.with(this)
                .load("POST", url)
                .setBodyParameter("email", email)
                .setBodyParameter("password", password)
                .setBodyParameter("firstname", firstname)
                .setBodyParameter("lastname", lastname)
                .setBodyParameter("tel", tel)
                .setBodyParameter("username", username)
                .setBodyParameter("floor", floor)
                .setBodyParameter("area", area)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e != null) {
                            Log.e(R.string.error_generic + "", R.string.error_network + "", e);
                            return;
                        }

                        if (result != null && result.contains("success")) {
                            Toast.makeText(RegisterActivity.this, R.string.success_account_created, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            try {
                                JSONObject jo = new JSONObject(result);
                                String error = jo.getString("error");
                                Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_SHORT).show();
                                Log.d("PB_ID", R.string.error_server + error);
                            }
                            catch (JSONException ex) {
                                Log.e("JSON_PARSE", R.string.error_json + result);
                            }
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
}