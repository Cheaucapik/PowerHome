package iut.dam.powerhome;

import android.os.Bundle;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class HabitatActivity extends AppCompatActivity {

    private ArrayList<Habitat> habitats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.habitatactivity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ListView habitantsLV = findViewById(R.id.lv_habitants);
        habitats = new ArrayList<>();


        HabitatAdapter adapter = new HabitatAdapter(this, R.layout.list_item, habitats);
        habitantsLV.setAdapter(adapter);
    }

    public void addHabitat(){
        habitats.add(new Habitat(1, "Gaëtan Leclair", 1, 0));
        habitats.add(new Habitat(2, "Cédric Boudet", 1, 0));
        habitats.add(new Habitat(3, "Adam Jacquinot", 1, 0));
        habitats.add(new Habitat(4, "Abel Fresnel", 1, 0));
    }
}