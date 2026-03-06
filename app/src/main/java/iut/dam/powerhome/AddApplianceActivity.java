package iut.dam.powerhome;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class AddApplianceActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.add_appliance_signup_activity);
        TextView name_tv = findViewById(R.id.name_tv);

        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        String json = sp.getString("user_json", null);

        if (json != null) {
            User currentUser = User.getFromJson(json);
            String name = currentUser.firstname + " " + currentUser.lastname;
            name_tv.setText(getString(R.string.welcome) + name);
        }
    }

    public void add(View v){
        CustomDialogAddAppliance custom = new CustomDialogAddAppliance(AddApplianceActivity.this);
        custom.show();
    }

    public void later(View v){
        Intent intent = new Intent(AddApplianceActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}