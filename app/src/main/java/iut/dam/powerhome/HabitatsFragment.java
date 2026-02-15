package iut.dam.powerhome;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;

public class HabitatsFragment extends Fragment {
    private ArrayList<Habitat> habitats;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.habitatactivity, container, false);

        ListView habitantsLV = rootView.findViewById(R.id.lv_habitants);
        habitats = new ArrayList<>();
        addHabitat();

        HabitatAdapter adapter = new HabitatAdapter(getContext(), R.layout.item_habitat, habitats);
        habitantsLV.setAdapter(adapter);

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

    public void addHabitat() {

        ArrayList<Appliance> gaetanApp = new ArrayList<>(Arrays.asList(
                new Appliance(1, "Machine à laver", "RML1", 3000, R.drawable.washing_machine),
                new Appliance(2, "Aspirateur", "RA1", 400, R.drawable.vacuum),
                new Appliance(3, "Climatiseur", "RC1", 2500, R.drawable.air_conditioning),
                new Appliance(4, "Repasseur vapeur", "RRV1", 1000, R.drawable.steam_iron)));
        habitats.add(new Habitat(1, "Gaëtan Leclair", 1, 42, gaetanApp));

        ArrayList<Appliance> cedricApp = new ArrayList<>(Arrays.asList(
                new Appliance(5, "Machine à laver", "RMV2", 3000, R.drawable.washing_machine)));
        habitats.add(new Habitat(2, "Cédric Boudet", 1, 54, cedricApp));

        ArrayList<Appliance> gaylordApp = new ArrayList<>(Arrays.asList(
                new Appliance(6, "Repasseur vapeur", "RRV2", 1000, R.drawable.steam_iron),
                new Appliance(7, "Aspirateur", "RA2", 400, R.drawable.vacuum)));
        habitats.add(new Habitat(3, "Gaylord Thibodeaux", 2, 41, gaylordApp));

        ArrayList<Appliance> adamApp = new ArrayList<>(Arrays.asList(
                new Appliance(8, "Machine à laver", "RMV3", 3000, R.drawable.washing_machine),
                new Appliance(9, "Repasseur vapeur", "RRV3", 1000, R.drawable.steam_iron),
                new Appliance(10, "Aspirateur", "RA3", 400, R.drawable.vacuum)));
        habitats.add(new Habitat(3, "Adam Jacquinot", 1, 34, adamApp));

        ArrayList<Appliance> abelApp = new ArrayList<>(Arrays.asList(
                new Appliance(11, "Aspirateur", "RA4", 400, R.drawable.vacuum)));
        habitats.add(new Habitat(4, "Abel Fresnel", 1, 49, abelApp));
    }
}
