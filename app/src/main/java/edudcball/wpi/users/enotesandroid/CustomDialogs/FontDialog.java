package edudcball.wpi.users.enotesandroid.CustomDialogs;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.RadioButton;

import java.util.HashMap;

import edudcball.wpi.users.enotesandroid.EventHandler;
import edudcball.wpi.users.enotesandroid.R;

/**
 * A dialog for selecting the font of a note
 */
public class FontDialog extends CustomOptionsDialog<String> {

    // Constructor
    public FontDialog(Context context, String selectedFont, EventHandler<String> confirmEvent) {
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
        buttonMap.put(res.getString(R.string.arial), (RadioButton)findViewById(R.id.arialButton));
        buttonMap.put(res.getString(R.string.palatino), (RadioButton)findViewById(R.id.palatinoButton));
        buttonMap.put(res.getString(R.string.courier), (RadioButton)findViewById(R.id.courierButton));

        selectOption();
    }
}
