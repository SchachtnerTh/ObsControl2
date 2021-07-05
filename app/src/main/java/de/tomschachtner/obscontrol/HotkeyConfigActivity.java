package de.tomschachtner.obscontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.ToggleButton;

public class HotkeyConfigActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hotkey_config);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        return super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.addNewHotkey:
                Toast.makeText(this, "Neuer Hotkey", Toast.LENGTH_SHORT);
                // neue Activity:
                //    Dropdown-Liste: alle noch verfügbaren Hotkeys
                //    Textfeld: Funktion, die der Hotkey ausführen soll
                //    Checkboxen: shift, alt, ctrl, cmd
                //    OK-Button, Cancel-Button
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_hotkey_config, menu);
        return true;
    }
}