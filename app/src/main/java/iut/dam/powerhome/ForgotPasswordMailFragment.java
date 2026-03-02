package iut.dam.powerhome;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

public class ForgotPasswordMailFragment extends Fragment {

    View layout;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.forgot_password_mail_fragment, container, false);

        return layout;
    }

    private boolean isValidEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void recover(View v){
        EditText email_et = layout.findViewById(R.id.email_et);
        String email = email_et.getText().toString().trim();

        String url = "http://10.0.2.2/powerhome_server/";

        ForgotPasswordActivity.code();
    }
}
