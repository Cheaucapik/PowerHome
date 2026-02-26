package iut.dam.powerhome;

import static java.security.AccessController.getContext;

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
        String lastname = ((EditText) findViewById(R.id.lastname_et)).getText().toString().trim();
        String firstname = ((EditText) findViewById(R.id.firstname_et)).getText().toString().trim();

        Spinner prefixe_sp = findViewById(R.id.sp_prefixe);
        String tel_brut = ((EditText) findViewById(R.id.tel_et)).getText().toString().trim();
        String tel = prefixe_sp.getSelectedItem().toString() + tel_brut;

        String url = "http://10.0.2.2/powerhome_server/signup.php?email=" + email
                + "&password=" + password
                + "&firstname=" + firstname
                + "&lastname=" + lastname
                + "&tel=" + tel
                + "&floor=" + floor
                + "&area=" + area;

        Log.d("DEBUG_URL", url);

        Ion.with(this)
                .load(url)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e != null) {
                            Log.e("Erreur", "Problème réseau");
                            return;
                        }
                        if (result != null && result.contains("success")) {
                            Toast.makeText(RegisterActivity.this, "Compte créé !", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Erreur : " + result, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}