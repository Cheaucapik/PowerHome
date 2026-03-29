package iut.dam.powerhome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.fragment.app.Fragment;

import com.koushikdutta.ion.Ion;

import org.json.JSONObject;

public class ParamFragment extends Fragment {

    private EditText etFname, etLname, etEmail, etMobile, etUsername;
    private Spinner spPrefixe;
    private String userToken;
    private final String[] items = {"+1", "+33", "+34"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.parametres_fragment, container, false);

        etFname    = layout.findViewById(R.id.et_firstname);
        etLname    = layout.findViewById(R.id.et_lastname);
        etEmail    = layout.findViewById(R.id.et_email);
        etMobile   = layout.findViewById(R.id.et_mobile);
        etUsername = layout.findViewById(R.id.et_username);
        spPrefixe  = layout.findViewById(R.id.sp_prefixe);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, items);
        spPrefixe.setAdapter(adapter);

        SharedPreferences sp = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userToken = sp.getString("token", "");

        etFname.setText(sp.getString("firstname", "").replace("null", ""));
        etLname.setText(sp.getString("lastname", "").replace("null", ""));
        etEmail.setText(sp.getString("email", "").replace("null", ""));
        etUsername.setText(sp.getString("username", "").replace("null", ""));

        String fullTel = sp.getString("tel", "").replace("null", "").trim();

        if (fullTel.contains("-")) {
            String[] parts = fullTel.split("-");
            String prefix = parts[0].trim();
            String number = (parts.length > 1) ? parts[1].trim() : "";

            etMobile.setText(number);

            spPrefixe.post(() -> {
                for (int i = 0; i < items.length; i++) {
                    String cleanItem = items[i].replace("+", "").trim();
                    String cleanPrefix = prefix.replace("+", "").trim();

                    if (cleanItem.equals(cleanPrefix)) {
                        spPrefixe.setSelection(i);
                        break;
                    }
                }
            });
        } else {
            etMobile.setText(fullTel);
        }

        layout.findViewById(R.id.btn_save).setOnClickListener(v -> showConfirmationDialog());
        layout.findViewById(R.id.btn_change_password).setOnClickListener(v -> showPasswordDialog());

        return layout;
    }

    private void showConfirmationDialog() {
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.Confirmation)
                .setMessage(R.string.edit_profile)
                .setPositiveButton(R.string.btn_yes, null)
                .setNegativeButton(R.string.btn_cancel, null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String telComplet = spPrefixe.getSelectedItem().toString() + "-" + etMobile.getText().toString();

            if (!isValidEmail(email)) {
                etEmail.setError(R.string.error_invalid_email + "");
                etEmail.requestFocus();
                dialog.dismiss();
            } else {
                updateUser(
                        etFname.getText().toString().trim(),
                        etLname.getText().toString().trim(),
                        email,
                        telComplet,
                        etUsername.getText().toString().trim(),
                        "",
                        ""
                );
                dialog.dismiss();
            }
        });
    }

    private void showPasswordDialog() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.change_password, null);
        EditText oldP = v.findViewById(R.id.et_old_password);
        EditText newP = v.findViewById(R.id.et_new_password);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.dialog_change_password_title)
                .setView(v)
                .setPositiveButton(R.string.btn_ok, null)
                .setNegativeButton(R.string.btn_cancel, null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String password = newP.getText().toString().trim();

            if (!isValidPassword(password)) {
                newP.setError(R.string.error_password_regex + "");
                newP.requestFocus();
            } else {
                String telComplet = spPrefixe.getSelectedItem().toString() + "-" + etMobile.getText().toString();
                updateUser(
                        etFname.getText().toString().trim(),
                        etLname.getText().toString().trim(),
                        etEmail.getText().toString().trim(),
                        telComplet,
                        etUsername.getText().toString().trim(),
                        oldP.getText().toString(),
                        password
                );
                dialog.dismiss();
            }
        });
    }

    private void updateUser(String fname, String lname, String email, String tel, String user, String oldP, String newP) {
        Ion.with(this)
                .load("http://10.0.2.2/www/update_user.php")
                .setBodyParameter("token", userToken)
                .setBodyParameter("firstname", fname)
                .setBodyParameter("lastname", lname)
                .setBodyParameter("email", email)
                .setBodyParameter("tel", tel)
                .setBodyParameter("username", user)
                .setBodyParameter("old_password", oldP)
                .setBodyParameter("new_password", newP)
                .asString()
                .setCallback((e, result) -> {
                    if (e != null) {
                        Log.e("Erreur", "Update echoué", e);
                        return;
                    }
                    try {
                        JSONObject jo = new JSONObject(result);
                        if (jo.optString("status").equals("success")) {
                            SharedPreferences sp = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                            sp.edit()
                                    .putString("firstname", fname)
                                    .putString("lastname", lname)
                                    .putString("email", email)
                                    .putString("tel", tel)
                                    .putString("username", user)
                                    .apply();
                            Toast.makeText(getContext(), getString(R.string.success_profile_updated), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), jo.optString("message", "Erreur"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception ex) {
                        Toast.makeText(getContext(), "Erreur serveur", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isValidEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$";
        return password != null && password.matches(regex);
    }
}