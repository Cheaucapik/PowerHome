package iut.dam.powerhome;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class HabitatAdapter extends ArrayAdapter<Habitat> {
    private ArrayList<Habitat> habitatList;

    public HabitatAdapter(@NonNull Context context, int resource, ArrayList<Habitat> habitatList) {
        super(context, resource);
        this.habitatList = habitatList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        int index = position;
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        return convertView;
    }

}
