package iut.dam.powerhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.Map;

public class ParamFragment extends Fragment {

    private EditText etFname, etLname, etEmail, etPass, etMobile;
    private String userToken;

    public ParamFragment(){ }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.parametres_fragment, container, false);

        // 1. Initialisation des composants
        etFname = layout.findViewById(R.id.et_firstname);
        etLname = layout.findViewById(R.id.et_lastname);
        etEmail = layout.findViewById(R.id.et_email);
        etPass = layout.findViewById(R.id.et_password);
        etMobile = layout.findViewById(R.id.et_mobile);
        Spinner prefixeSP = layout.findViewById(R.id.sp_prefixe);

        // 2. Pré-remplissage avec la session actuelle
        SharedPreferences sp = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        userToken = sp.getString("token", "");
        etFname.setText(sp.getString("firstname", ""));
        etLname.setText(sp.getString("lastname", ""));
        etEmail.setText(sp.getString("email", ""));

        // Spinner
        String[] items = {"+1", "+33", "+34"};
        prefixeSP.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, items));

        // 3. Clic sur Enregistrer
        layout.findViewById(R.id.btn_save).setOnClickListener(v -> {
            saveChanges(etFname.getText().toString(), etLname.getText().toString(),
                    etEmail.getText().toString(), etPass.getText().toString());
        });

        return layout;
    }

    private void saveChanges(String fn, String ln, String em, String ps) {
        String url = "http://10.0.2.2/powerhome_server/update_user.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Mise à jour locale pour que MainActivity se rafraîchisse
                    SharedPreferences sp = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("firstname", fn);
                    editor.putString("lastname", ln);
                    editor.putString("email", em);
                    editor.apply();
                    Toast.makeText(getContext(), "Modifications enregistrées !", Toast.LENGTH_SHORT).show();
                },
                error -> Toast.makeText(getContext(), "Erreur réseau uWamp", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("token", userToken);
                params.put("firstname", fn);
                params.put("lastname", ln);
                params.put("email", em);
                params.put("password", ps);
                return params;
            }
        };
        Volley.newRequestQueue(getContext()).add(request);
    }
}