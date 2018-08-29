package edudcball.wpi.users.enotesandroid.CustomDialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;

import java.util.Map;

import edudcball.wpi.users.enotesandroid.R;
import edudcball.wpi.users.enotesandroid.Settings;

public class SettingsDialog extends Dialog {

    Context context;
    private Button applyButton, cancelButton;
    //private WindowManager.LayoutParams params;
    int width, height;

    public SettingsDialog(Context context) {
        super(context);

        this.context = context;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        width = (int)(displayMetrics.widthPixels * 0.8);
        height = (int)(displayMetrics.heightPixels * 0.8);
        params.width = width;
        params.height = height;
        getWindow().setAttributes(params);
    }

    /**
     * Called when the dialog is opened
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // load visuals
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_settings);

        applyButton = findViewById(R.id.applyButton);
        cancelButton = findViewById(R.id.cancelButton);

        final RadioGroup iconSizeGroup = findViewById(R.id.iconSizeGroup);

        // set apply button action
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RadioButton selectedRB = findViewById(iconSizeGroup.getCheckedRadioButtonId());

                switch(selectedRB.getText().toString()){
                    case "Small":
                        Settings.setIconSize(Settings.Size.SMALL);
                        break;
                    case "Medium":
                    default:
                        Settings.setIconSize(Settings.Size.MEDIUM);
                        break;
                    case "Large":
                        Settings.setIconSize(Settings.Size.LARGE);
                        break;
                }
                // close dialog
                dismiss();
            }
        });

        // have the cancel button simply close the dialog
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

    }
}
