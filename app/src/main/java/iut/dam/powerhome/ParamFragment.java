package iut.dam.powerhome;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

public class ParamFragment extends Fragment {

    public ParamFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.parametres_fragment, container, false);

        Spinner prefixeSP = layout.findViewById(R.id.sp_prefixe);
        String[] items = {"+1", "+33", "+34"};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_list_item_1,
                        items);
        prefixeSP.setAdapter(adapter);

        return layout;
    }
}