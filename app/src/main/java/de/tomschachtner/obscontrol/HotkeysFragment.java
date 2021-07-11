package de.tomschachtner.obscontrol;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import de.tomschachtner.obscontrol.util.HotkeyItemTouchHelper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HotkeysFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HotkeysFragment extends Fragment implements  OBSHotkeysButtonsAdapter.OnHotkeyClickListener {

    MainActivity theActivity;
    RecyclerView obsHotkeysButtons;
    OBSHotkeysButtonsAdapter hotkeysButtonsAdapter;

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
        FrameLayout cl = (FrameLayout)inflater.inflate(R.layout.fragment_hotkeys, container, false);
        // link variables to the UI elements
        obsHotkeysButtons = cl.findViewById(R.id.hotkeys_button_list);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        String strColumns = sp.getString("columns", "4");

        int numberOfHotkeysColumns = Integer.parseInt(strColumns);
        obsHotkeysButtons.setLayoutManager(new GridLayoutManager(getContext(), numberOfHotkeysColumns));
        hotkeysButtonsAdapter = new OBSHotkeysButtonsAdapter(getContext());
        hotkeysButtonsAdapter.setHotkeyClickListener(this);
        //theActivity.mOBSWebSocketClient.setOnObsHotkeysChangedListener(sceneButtonsAdapter);

        ItemTouchHelper.Callback callback = new HotkeyItemTouchHelper(hotkeysButtonsAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        hotkeysButtonsAdapter.setTouchHelper(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(obsHotkeysButtons);

        obsHotkeysButtons.setAdapter(hotkeysButtonsAdapter);
        return cl;
    }

    @Override
    public void onHotkeyClick(int position) {
        OBSHotkeysDatabaseHelper dbHelper = new OBSHotkeysDatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        theActivity.mOBSWebSocketClient.sendHotkey(
                dbHelper.getHotkeyKey(db, position),
                dbHelper.getHotkeyModifier(db, 1, position),
                dbHelper.getHotkeyModifier(db,2,position),
                dbHelper.getHotkeyModifier(db, 3,position) ,
                dbHelper.getHotkeyModifier(db,4,position));
    }
}