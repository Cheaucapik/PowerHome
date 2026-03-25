package iut.dam.powerhome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ParamFragment extends Fragment {

    private EditText etFname, etLname, etEmail, etMobile, etUsername;
    private Spinner spPrefixe;
    private String userToken;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View layout = inflater.inflate(R.layout.parametres_fragment, container, false);

        etFname    = layout.findViewById(R.id.et_firstname);
        etLname    = layout.findViewById(R.id.et_lastname);
        etEmail    = layout.findViewById(R.id.et_email);
        etMobile   = layout.findViewById(R.id.et_mobile);
        etUsername = layout.findViewById(R.id.et_username);
        spPrefixe  = layout.findViewById(R.id.sp_prefixe);

        String[] items = {"+1", "+33", "+34"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPrefixe.setAdapter(adapter);

        SharedPreferences sp = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userToken = sp.getString("token", "");

        String firstname = sp.getString("firstname", "");
        if ("null".equals(firstname)) firstname = "";

        String lastname = sp.getString("lastname", "");
        if ("null".equals(lastname)) lastname = "";

        String email = sp.getString("email", "");
        if ("null".equals(email)) email = "";

        String username = sp.getString("username", "");
        if ("null".equals(username)) username = "";

        String tel = sp.getString("tel", "");
        if ("null".equals(tel)) tel = "";

        etFname.setText(firstname);
        etLname.setText(lastname);
        etEmail.setText(email);
        etUsername.setText(username);
        etMobile.setText(tel);

        layout.findViewById(R.id.btn_save).setOnClickListener(v -> showConfirmationDialog());
        layout.findViewById(R.id.btn_change_password).setOnClickListener(v -> showPasswordDialog());

        return layout;
    }


    private void showConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.dialog_confirm_title))
                .setMessage(getString(R.string.dialog_confirm_profile_msg))
                .setPositiveButton(getString(R.string.btn_yes), (d, w) -> {
                    new UpdateUserTask().execute(
                            userToken,
                            etFname.getText().toString(),
                            etLname.getText().toString(),
                            etEmail.getText().toString(),
                            spPrefixe.getSelectedItem().toString() + etMobile.getText().toString(),
                            etUsername.getText().toString(),
                            "",
                            ""
                    );
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showPasswordDialog() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.change_password, null);

        EditText oldP = v.findViewById(R.id.et_old_password);
        EditText newP = v.findViewById(R.id.et_new_password);

        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.dialog_change_password_title))
                .setView(v)
                .setPositiveButton(getString(R.string.btn_ok), (d, w) -> {
                    new UpdateUserTask().execute(
                            userToken,
                            etFname.getText().toString(),
                            etLname.getText().toString(),
                            etEmail.getText().toString(),
                            spPrefixe.getSelectedItem().toString() + etMobile.getText().toString(),
                            etUsername.getText().toString(),
                            oldP.getText().toString(),
                            newP.getText().toString()
                    );
                })
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    private class UpdateUserTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... p) {
            try {
                URL url = new URL("http://10.0.2.2/powerhome_server/update_user.php");
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("POST");
                c.setDoOutput(true);

                String data =
                        "token=" + URLEncoder.encode(p[0], "UTF-8") +
                                "&firstname=" + URLEncoder.encode(p[1], "UTF-8") +
                                "&lastname=" + URLEncoder.encode(p[2], "UTF-8") +
                                "&email=" + URLEncoder.encode(p[3], "UTF-8") +
                                "&tel=" + URLEncoder.encode(p[4], "UTF-8") +
                                "&username=" + URLEncoder.encode(p[5], "UTF-8");

                OutputStream os = c.getOutputStream();
                os.write(data.getBytes());
                os.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                return br.readLine();

            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(String r) {
            Toast.makeText(getContext(), getString(R.string.success_profile_updated), Toast.LENGTH_SHORT).show();        }
    }
}