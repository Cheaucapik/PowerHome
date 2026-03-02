package iut.dam.powerhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.koushikdutta.ion.Ion;
import org.json.JSONObject;

public class ParamFragment extends Fragment {

    private EditText etFname, etLname, etEmail, etPass;
    private String userToken;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.parametres_fragment, container, false);

        etFname = layout.findViewById(R.id.et_firstname);
        etLname = layout.findViewById(R.id.et_lastname);
        etEmail = layout.findViewById(R.id.et_email);
        etPass = layout.findViewById(R.id.et_password);

        SharedPreferences sp = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        userToken = sp.getString("token", "");

        etFname.setText(sp.getString("firstname", ""));
        etLname.setText(sp.getString("lastname", ""));
        etEmail.setText(sp.getString("email", ""));

        layout.findViewById(R.id.btn_save).setOnClickListener(v -> saveWithIon());

        return layout;
    }

    private void saveWithIon() {
        String url = "http://10.0.2.2/powerhome_server/update_user.php";

        Ion.with(this)
                .load(url)
                .setBodyParameter("token", userToken)
                .setBodyParameter("firstname", etFname.getText().toString())
                .setBodyParameter("lastname", etLname.getText().toString())
                .setBodyParameter("email", etEmail.getText().toString())
                .setBodyParameter("password", etPass.getText().toString())
                .asString()
                .setCallback((e, result) -> {
                    if (e != null) {
                        Toast.makeText(getContext(), "Erreur réseau", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Mise à jour des SharedPreferences après succès
                    SharedPreferences sp = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                    sp.edit()
                            .putString("firstname", etFname.getText().toString())
                            .putString("lastname", etLname.getText().toString())
                            .putString("email", etEmail.getText().toString())
                            .apply();

                    Toast.makeText(getContext(), "Profil mis à jour !", Toast.LENGTH_SHORT).show();
                });
    }
}