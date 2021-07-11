package de.tomschachtner.obscontrol;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == 1); // TODO: refresh hotkey list shown
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        return super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.addNewHotkey:
                Intent i = new Intent(this, AddNewHotkeyActivity.class);
                startActivityForResult(i, 1);
                // Toast.makeText(this, "Neuer Hotkey", Toast.LENGTH_SHORT);
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