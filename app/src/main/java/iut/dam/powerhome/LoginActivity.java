package iut.dam.powerhome;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.loginactivity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginactivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void login(View v){
        EditText editEmail = findViewById(R.id.et_email);
        EditText editPassword = findViewById(R.id.et_password);

        String email = editEmail.getText().toString();
        String password = editPassword.getText().toString();

        if(password.equals("EFGH") && email.equals("abcd")){
//            Intent intent = new Intent(this, WelcomeActivity.class);

//            intent.putExtra("email", email);
//            intent.putExtra("mdp", password);

            Intent intent = new Intent(this, MainActivity.class);

            startActivity(intent);
        }
        else{
            Toast.makeText(this, "Identifiants incorrects", Toast.LENGTH_SHORT).show();
        }
    }
}
