package iut.dam.powerhome;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

public class MonHabitatFragment extends Fragment {

    private View layout;
    private ArrayList<Appliance> myAppliances = new ArrayList<>();
    private ApplianceAdapter adapter;
    private TextView name_tv, nb_appareil_tv, pseudo_tv, etage_tv,
            surface_tv, conso_tv, nodevice_tv, tv_solde;

    private User currentUser;

    public MonHabitatFragment() {}

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.monhabitat_fragment, container, false);

        name_tv        = layout.findViewById(R.id.name_tv);
        nb_appareil_tv = layout.findViewById(R.id.nb_appareil_tv);
        pseudo_tv      = layout.findViewById(R.id.pseudo_tv);
        etage_tv       = layout.findViewById(R.id.etage_tv);
        surface_tv     = layout.findViewById(R.id.surface_tv);
        conso_tv       = layout.findViewById(R.id.conso_tv);
        nodevice_tv    = layout.findViewById(R.id.nodevice_tv);
        tv_solde       = layout.findViewById(R.id.tv_solde);
        ListView lv    = layout.findViewById(R.id.lv_appliances);

        adapter = new ApplianceAdapter(requireContext(), R.layout.item_info, myAppliances);
        lv.setAdapter(adapter);

        SharedPreferences sp = getContext().getSharedPreferences("UserSession", MODE_PRIVATE);
        String json = sp.getString("user_json", null);

        if (json != null) {
            currentUser = User.getFromJson(json);
            name_tv.setText(currentUser.firstname + " " + currentUser.lastname);
            pseudo_tv.setText(currentUser.username);
            etage_tv.setText(String.valueOf(currentUser.habitat.floor));
            surface_tv.setText(currentUser.habitat.area + " m²");

            // Display solde from session (already up-to-date after reservations)
            displaySolde(currentUser.solde);

            loadDataFromServer(currentUser.email);
        } else {
            Toast.makeText(getContext(), R.string.error_no_user_connected, Toast.LENGTH_SHORT).show();
        }

        ImageView edit_iv = layout.findViewById(R.id.iv_edit);
        edit_iv.setOnClickListener(v -> {
            CustomDialogEditAppliance cs = new CustomDialogEditAppliance(getContext(), myAppliances, adapter);
            cs.setOnDismissListener(dialog -> update());
            cs.show();
        });

        return layout;
    }

    /**
     * Called every time the fragment becomes visible (e.g. after navigating back from
     * My Requests where a reservation was confirmed and the solde was updated in session).
     */
    @Override
    public void onResume() {
        super.onResume();
        refreshSoldeFromSession();
    }

    /** Re-read solde from SharedPreferences and refresh the view. */
    @SuppressLint("SetTextI18n")
    private void refreshSoldeFromSession() {
        if (getContext() == null) return;
        SharedPreferences sp = getContext().getSharedPreferences("UserSession", MODE_PRIVATE);
        String json = sp.getString("user_json", null);
        if (json == null) return;
        currentUser = User.getFromJson(json);
        displaySolde(currentUser.solde);
    }

    @SuppressLint("SetTextI18n")
    private void displaySolde(int solde) {
        if (tv_solde == null) return;
        tv_solde.setText(String.valueOf(solde));
        if (solde > 0) {
            tv_solde.setTextColor(ContextCompat.getColor(requireContext(), R.color.green));
        } else if (solde < 0) {
            tv_solde.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
        } else {
            tv_solde.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
        }
    }

    private void loadDataFromServer(String email) {
        String url = "http://10.0.2.2/www/getHabitatByUser.php?email=" + email;

        Ion.with(this)
                .load(url)
                .asString()
                .setCallback((e, result) -> {
                    if (e != null) {
                        Log.e(R.string.myAccomodation + "", R.string.error_network + "", e);
                        return;
                    }
                    if (result != null) {
                        Habitat habitatUser = Habitat.getFromJson(result);
                        if (habitatUser != null) {
                            myAppliances.clear();
                            myAppliances.addAll(habitatUser.getAppliances());
                            update();
                        }
                    }
                });
    }

    public void update() {
        if (getContext() == null || layout == null) return;

        int sum = 0;
        for (Appliance a : myAppliances) sum += a.wattage;

        conso_tv.setText(sum + " W");

        int size = myAppliances.size();
        String label = getContext().getString(R.string.appliance) + (size > 1 ? "s" : "");
        nb_appareil_tv.setText(size + " " + label);

        nodevice_tv.setVisibility(size == 0 ? View.VISIBLE : View.GONE);
        if (size == 0) {
            nodevice_tv.setText(getContext().getString(R.string.you_don_t_have_any_appliance_yet));
        }

        if (adapter != null) adapter.notifyDataSetChanged();
    }
}