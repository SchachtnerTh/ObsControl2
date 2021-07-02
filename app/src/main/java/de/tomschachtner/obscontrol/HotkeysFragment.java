package de.tomschachtner.obscontrol;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HotkeysFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HotkeysFragment extends Fragment {

    MainActivity theActivity;
    RecyclerView obsHotkeysButtons;

    public HotkeysFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        theActivity = (MainActivity)getActivity();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ConstraintLayout cl = (ConstraintLayout)inflater.inflate(R.layout.fragment_hotkeys, container, false);
        // link variables to the UI elements
        obsHotkeysButtons = cl.findViewById(R.id.scenes_button_list);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        String strColumns = sp.getString("columns", "4");

        int numberOfHotkeysColumns = Integer.parseInt(strColumns);
        obsHotkeysButtons.setLayoutManager(new GridLayoutManager(getContext(), numberOfHotkeysColumns));
        hotkeysButtonsAdapter = new OBSHotkeysButtonsAdapter(getContext(), theActivity.mOBSWebSocketClient.obsScenes);
        hotkeysButtonsAdapter.setSceneClickListener(this);
        //theActivity.mOBSWebSocketClient.setOnObsHotkeysChangedListener(sceneButtonsAdapter);
        obsHotkeysButtons.setAdapter(hotkeysButtonsAdapter);
        return cl;
    }
}