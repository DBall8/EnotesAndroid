package edudcball.wpi.users.enotesandroid.CustomDialogs;

import android.content.Context;
import android.os.Bundle;
import android.widget.RadioButton;

import java.util.HashMap;

import edudcball.wpi.users.enotesandroid.EventHandler;
import edudcball.wpi.users.enotesandroid.R;

public class FontSizeDialog extends CustomOptionsDialog {

    public FontSizeDialog(Context context, int selectedFontSize, EventHandler<Integer> confirmEvent) {
        super(context, R.layout.dialog_font_size, selectedFontSize, confirmEvent);
    }

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
