package de.tomschachtner.obscontrol;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class AvailableHotkeysCursorAdapter extends CursorAdapter {
    public AvailableHotkeysCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.support_simple_spinner_dropdown_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvText = (TextView)view.findViewById(android.R.id.text1);

        String hotkey = cursor.getString(cursor.getColumnIndexOrThrow(OBSHotkeysDBContract.OBSHotkeyList.COLUMN_NAME_HOTKEY));
        tvText.setText(hotkey);
    }
}
