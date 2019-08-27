package edudcball.wpi.users.enotesandroid.Old.CustomDialogs;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.RadioButton;

import java.util.HashMap;

import edudcball.wpi.users.enotesandroid.EventHandler;
import edudcball.wpi.users.enotesandroid.R;
import edudcball.wpi.users.enotesandroid.Old.noteDataTypes.NoteLookupTable;

/**
 * A dialog for selecting the font of a note
 */
public class FontDialog extends CustomOptionsDialog<NoteLookupTable.NoteFont> {

    // Constructor
    public FontDialog(Context context, NoteLookupTable.NoteFont selectedFont, EventHandler<NoteLookupTable.NoteFont> confirmEvent) {
        super(context, R.layout.dialog_font, selectedFont, confirmEvent);
    }

    /**
     * Sets each radio button to bind to a certain font
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buttonMap = new HashMap<>();

        Resources res = context.getResources();
        buttonMap.put(NoteLookupTable.NoteFont.ARIAL, (RadioButton)findViewById(R.id.arialButton));
        buttonMap.put(NoteLookupTable.NoteFont.PALATINO, (RadioButton)findViewById(R.id.palatinoButton));
        buttonMap.put(NoteLookupTable.NoteFont.COURIER, (RadioButton)findViewById(R.id.courierButton));

        selectOption();
    }
}
