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

    public void signin(View v){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void signup(View v){

        EditText email_et = findViewById(R.id.email_et);
        EditText password_et = findViewById(R.id.password_et);
        EditText tel_et = findViewById(R.id.tel_et);
        EditText lastname_et = findViewById(R.id.lastname_et);
        EditText firstname_et = findViewById(R.id.firstname_et);

        Spinner prefixe_sp = findViewById(R.id.sp_prefixe);

        String email = email_et.getText().toString().trim();
        String password = password_et.getText().toString().trim();
        String lastname = lastname_et.getText().toString().trim();
        String firstname = firstname_et.getText().toString().trim();

        String tel = tel_et.getText().toString().trim() + prefixe_sp.getSelectedItem().toString().trim();


        String url = "http://10.0.2.2/powerhome_server/signup.php?email=" + email + "&password=" + password + "&firstname=" + firstname + "&lastname=" + lastname + "&tel=" + tel;

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

                        Log.d("DEBUG_SERVER", "Réponse du PHP : " + result);

                        if (result != null && result.contains("success")) {
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                        else{
                            Toast.makeText(RegisterActivity.this,  result, Toast.LENGTH_SHORT).show();
                            Log.d("PB_ID", "Serveur : " + result);
                        }
                    }
                });
    }
}
