package iut.dam.powerhome;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static FragmentManager fm;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_activity);

        fm = getSupportFragmentManager();

        ImageView retour = findViewById(R.id.back_iv);
        retour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                } else {
                    finish();
                }
            }
        });

        fm.beginTransaction().replace(R.id.contentFL, new ForgotPasswordMailFragment()).commit();
    }

    public static void code(){
        fm.beginTransaction().addToBackStack(null).replace(R.id.contentFL, new ForgotPasswordCodeFragment()).commit();
    }

    public static void verify(){
        fm.beginTransaction().addToBackStack(null).replace(R.id.contentFL, new ForgotPasswordFragment()).commit();
    }

    public void done(){
        Toast.makeText(this, "Mot de passe réinitialisé", Toast.LENGTH_SHORT).show();
        finish();
    }
}
