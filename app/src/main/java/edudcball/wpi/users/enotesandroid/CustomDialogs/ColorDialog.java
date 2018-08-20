package edudcball.wpi.users.enotesandroid.CustomDialogs;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.RadioButton;

import org.json.JSONException;

import java.util.HashMap;

import edudcball.wpi.users.enotesandroid.EventHandler;
import edudcball.wpi.users.enotesandroid.R;

/**
 * A class for creating a dialog for selecting a color
 */
public class ColorDialog extends CustomOptionsDialog<Integer> {

    /**
     * Constructor
     * @param context the context the dialog is being opened in
     * @param selectedColor the color the note is currently
     * @param confirmEvent the event for running when the apply button is pressed
     */
    public ColorDialog(Context context, int selectedColor, EventHandler<Integer> confirmEvent) {
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
        buttonMap.put(ContextCompat.getColor(context, R.color.noteYellow), (RadioButton)findViewById(R.id.yellowButton));
        buttonMap.put(ContextCompat.getColor(context, R.color.noteOrange), (RadioButton)findViewById(R.id.orangeButton));
        buttonMap.put(ContextCompat.getColor(context, R.color.noteRed), (RadioButton)findViewById(R.id.redButton));
        buttonMap.put(ContextCompat.getColor(context, R.color.noteGreen), (RadioButton)findViewById(R.id.greenButton));
        buttonMap.put(ContextCompat.getColor(context, R.color.noteBlue), (RadioButton)findViewById(R.id.blueButton));
        buttonMap.put(ContextCompat.getColor(context, R.color.notePurple), (RadioButton)findViewById(R.id.purpleButton));

        // select the current color radio button
        selectOption();
    }
}
