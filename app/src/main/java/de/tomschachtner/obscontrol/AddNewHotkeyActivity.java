package de.tomschachtner.obscontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class AddNewHotkeyActivity extends AppCompatActivity {

    Spinner availableHotkeysSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_hotkey);

        availableHotkeysSpinner = findViewById(R.id.availableHotkeysSpinner);

        OBSHotkeysDatabaseHelper dbHelper = new OBSHotkeysDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = dbHelper.getAvailableHotkeys(db);

        // Populate spinner control with values
        CursorAdapter adapter = new AvailableHotkeysCursorAdapter(this, c, 0);

        availableHotkeysSpinner.setAdapter(adapter);

        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnCancel = findViewById(R.id.btnCancel);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int newId = dbHelper.getMaxItemNumber(db);
                CheckBox cbShift = findViewById(R.id.shift);
                CheckBox cbAlt = findViewById(R.id.alt);
                CheckBox cbCtrl = findViewById(R.id.ctrl);
                CheckBox cbCmd = findViewById(R.id.command);
                EditText tbHotkeyName = findViewById(R.id.tbHotkeyName);
                boolean result = dbHelper.addNewHotkey(
                        db,
                        (String) ((TextView)availableHotkeysSpinner.getSelectedView()).getText(),
                        tbHotkeyName.getText().toString(),
                        newId,
                        cbShift.isChecked(),
                        cbAlt.isChecked(),
                        cbCtrl.isChecked(),
                        cbCmd.isChecked());
                if (result)
                    setResult(1);
                else
                    setResult(0);
                finish();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(0);
                finish();
            }
        });
    }
}