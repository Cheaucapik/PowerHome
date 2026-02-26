package iut.dam.powerhome;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.Arrays;

public class HabitatsFragment extends Fragment {
    private ArrayList<Habitat> habitats =new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.habitats_fragment, container, false);

        ListView habitantsLV = rootView.findViewById(R.id.lv_habitants);

        HabitatAdapter adapter = new HabitatAdapter(getContext(), R.layout.item_habitat, habitats);
        habitantsLV.setAdapter(adapter);

        String url = "http://10.0.2.2/powerhome_server/getHabitats.php";

        Ion.with(this)
                .load(url)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e != null) {
                            Log.e("Erreur", "Problème réseau");
                            return;
                        }
                        if (result != null) {
                            habitats.clear();
                            habitats.addAll(Habitat.getListFromJson(result));
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "Erreur : " + result, Toast.LENGTH_LONG).show();
                        }
                    }
                });

        habitantsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Habitat h = habitats.get(position);
                CustomDialog customDialog = new CustomDialog(getContext(), h);
                customDialog.show();
            }
        });

        return rootView;
    }
}
