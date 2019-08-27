package edudcball.wpi.users.enotesandroid.Old.CustomDialogs;

import android.content.Context;
import android.os.Bundle;
import android.widget.RadioButton;

import java.util.HashMap;

import edudcball.wpi.users.enotesandroid.EventHandler;
import edudcball.wpi.users.enotesandroid.R;

/**
 * A dialog for selecting the font size of the note
 */
public class FontSizeDialog extends CustomOptionsDialog {

    // Constructor
    public FontSizeDialog(Context context, int selectedFontSize, EventHandler<Integer> confirmEvent) {
        super(context, R.layout.dialog_font_size, selectedFontSize, confirmEvent);
    }

    /**
     * Binds each gont size to a radio button on creation
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buttonMap = new HashMap<>();
        buttonMap.put(10, (RadioButton)findViewById(R.id.tenButton));
        buttonMap.put(12, (RadioButton)findViewById(R.id.twelveButton));
        buttonMap.put(14, (RadioButton)findViewById(R.id.fourteenButton));
        buttonMap.put(18, (RadioButton)findViewById(R.id.eighteenButton));
        buttonMap.put(24, (RadioButton)findViewById(R.id.twentyFourButton));
        buttonMap.put(32, (RadioButton)findViewById(R.id.thirtyTwoButton));

        selectOption();
    }
}
