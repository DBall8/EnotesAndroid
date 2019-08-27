package edudcball.wpi.users.enotesandroid.Old.CustomDialogs;

import android.content.Context;
import android.os.Bundle;
import android.widget.RadioButton;

import java.util.HashMap;

import edudcball.wpi.users.enotesandroid.EventHandler;
import edudcball.wpi.users.enotesandroid.Old.noteDataTypes.NoteLookupTable;
import edudcball.wpi.users.enotesandroid.R;

/**
 * A class for creating a dialog for selecting a color
 */
public class ColorDialog extends CustomOptionsDialog<NoteLookupTable.NoteColor> {

    /**
     * Constructor
     * @param context the context the dialog is being opened in
     * @param selectedColor the color the note is currently
     * @param confirmEvent the event for running when the apply button is pressed
     */
    public ColorDialog(Context context, NoteLookupTable.NoteColor selectedColor, EventHandler<NoteLookupTable.NoteColor> confirmEvent) {
        // build the dialog with the color layout
        super(context, R.layout.dialog_color, selectedColor, confirmEvent);
    }

    /**
     * override onCreate to set all the radio buttons to match a certain color
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // associate each radio button with a color code
        buttonMap = new HashMap<>();
        buttonMap.put(NoteLookupTable.NoteColor.YELLOW, (RadioButton)findViewById(R.id.yellowButton));
        buttonMap.put(NoteLookupTable.NoteColor.ORANGE, (RadioButton)findViewById(R.id.orangeButton));
        buttonMap.put(NoteLookupTable.NoteColor.RED, (RadioButton)findViewById(R.id.redButton));
        buttonMap.put(NoteLookupTable.NoteColor.GREEN, (RadioButton)findViewById(R.id.greenButton));
        buttonMap.put(NoteLookupTable.NoteColor.BLUE, (RadioButton)findViewById(R.id.blueButton));
        buttonMap.put(NoteLookupTable.NoteColor.PURPLE, (RadioButton)findViewById(R.id.purpleButton));

        // select the current color radio button
        selectOption();
    }
}
