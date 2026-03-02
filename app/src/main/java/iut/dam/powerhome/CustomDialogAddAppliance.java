package iut.dam.powerhome;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

public class CustomDialogAddAppliance extends Dialog {

    public CustomDialogAddAppliance(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_info_add_appliance);
    }
}
