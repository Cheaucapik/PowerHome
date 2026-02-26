package iut.dam.powerhome;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

public class CustomDialogSignUp extends Dialog {
    private RegisterActivity myActivity; // On précise le type exact

    public CustomDialogSignUp(RegisterActivity context) {
        super(context);
        this.myActivity = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_info_signup);

        findViewById(R.id.btn_signup).setOnClickListener(view -> {
            EditText floorEt = findViewById(R.id.floor_et);
            EditText areaEt = findViewById(R.id.area_et);

            String floor = floorEt.getText().toString().trim();
            String area = areaEt.getText().toString().trim();

            if (floor.isEmpty() || area.isEmpty()) {
                Toast.makeText(getContext(), "Remplissez les infos habitat", Toast.LENGTH_SHORT).show();
            } else {
                myActivity.sendDataToServer(floor, area);
                dismiss();
            }
        });
    }
}
