package edudcball.wpi.users.enotesandroid.CustomDialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import edudcball.wpi.users.enotesandroid.activities.MainActivity;
import edudcball.wpi.users.enotesandroid.noteDataTypes.NoteLookupTable;
import edudcball.wpi.users.enotesandroid.EventHandler;
import edudcball.wpi.users.enotesandroid.R;
import edudcball.wpi.users.enotesandroid.Settings;

public class SettingsDialog extends Dialog {

    Context context;
    private Button applyButton, cancelButton;

    private RadioGroup iconSizeGroup;
    private RadioGroup textSizeGroup;
    private RadioGroup dColorGroup;
    private RadioGroup dFontGroup;
    private RadioGroup dFontSizeGroup;

    private EventHandler<Void> finishedEvent;

    private Settings settings;


    public SettingsDialog(Context context, EventHandler<Void> finishedEvent) {
        super(context);

        settings = MainActivity.getSettings();

        this.finishedEvent = finishedEvent;
        this.context = context;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        params.width = (int)(displayMetrics.widthPixels * 0.8);
        params.height = (int)(displayMetrics.heightPixels * 0.8);
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

        iconSizeGroup = findViewById(R.id.iconSizeGroup);
        textSizeGroup = findViewById(R.id.textSizeGroup);
        dColorGroup = findViewById(R.id.dColorGroup);
        dFontGroup = findViewById(R.id.dFontGroup);
        dFontSizeGroup = findViewById(R.id.dFontSizeGroup);

        setChecks();

        // set apply button action
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                updateSettings();

                // close dialog
                finishedEvent.handle(null);
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

    private void updateSettings(){
        RadioButton selectedRB = findViewById(iconSizeGroup.getCheckedRadioButtonId());
        switch(selectedRB.getText().toString()){
            case "Small":
                settings.setIconSize(Settings.Size.SMALL);
                break;
            case "Medium":
            default:
                settings.setIconSize(Settings.Size.MEDIUM);
                break;
            case "Large":
                settings.setIconSize(Settings.Size.LARGE);
                break;
        }

        selectedRB = findViewById(textSizeGroup.getCheckedRadioButtonId());
        switch(selectedRB.getText().toString()){
            case "Small":
                settings.setTextSize(Settings.Size.SMALL);
                break;
            case "Medium":
            default:
                settings.setTextSize(Settings.Size.MEDIUM);
                break;
            case "Large":
                settings.setTextSize(Settings.Size.LARGE);
                break;
        }

        selectedRB = findViewById(dColorGroup.getCheckedRadioButtonId());
        settings.setDefaultColor(NoteLookupTable.getColorFromStr(selectedRB.getText().toString()));

        selectedRB = findViewById(dFontGroup.getCheckedRadioButtonId());
        settings.setDefaultFont(NoteLookupTable.getFontFromStr(selectedRB.getText().toString()));

        selectedRB = findViewById(dFontSizeGroup.getCheckedRadioButtonId());
        try{
            settings.setDefaultFontSize(Integer.parseInt(selectedRB.getText().toString()));
        } catch (Exception e){
            Log.d("MYAPP", "Could not parse integer: " + selectedRB.getText());
        }

        settings.updateSettingsServerSide();
    }

    private void setChecks(){
        RadioButton radioButton;
        switch(settings.getIconSize()){
            case SMALL:
                radioButton = findViewById(R.id.smallIconRB);
                break;
            case MEDIUM:
            default:
                radioButton = findViewById(R.id.mediumIconRB);
                break;
            case LARGE:
                radioButton = findViewById(R.id.largeIconRB);
                break;
        }
        radioButton.setChecked(true);

        switch(settings.getTextSize()){
            case SMALL:
                radioButton = findViewById(R.id.smallTextRB );
                break;
            case MEDIUM:
            default:
                radioButton = findViewById(R.id.mediumTextRB);
                break;
            case LARGE:
                radioButton = findViewById(R.id.largeTextRB);
                break;
        }
        radioButton.setChecked(true);

        switch (settings.getDefaultColor()){
            default:
            case YELLOW:
                radioButton = findViewById(R.id.yellowButton);
                break;
            case ORANGE:
                radioButton = findViewById(R.id.orangeButton);
                break;
            case RED:
                radioButton = findViewById(R.id.redButton);
                break;
            case GREEN:
                radioButton = findViewById(R.id.greenButton);
                break;
            case BLUE:
                radioButton = findViewById(R.id.blueButton);
                break;
            case PURPLE:
                radioButton = findViewById(R.id.purpleButton);
                break;
        }
        radioButton.setChecked(true);

        switch(settings.getDefaultFont()){
            default:
            case ARIAL:
                radioButton = findViewById(R.id.arialRadioButton);
                break;
            case PALATINO:
                radioButton = findViewById(R.id.palatinoRadioButton);
                break;
            case COURIER:
                radioButton = findViewById(R.id.courierRadioButton);
                break;
        }

        radioButton.setChecked(true);

        switch (settings.getDefaultFontSize()){
            case 10:
                radioButton = findViewById(R.id.tenRadioButton);
                break;
            default:
            case 12:
                radioButton = findViewById(R.id.twelveRadioButton);
                break;
            case 14:
                radioButton = findViewById(R.id.fourteenRadioButton);
                break;
            case 18:
                radioButton = findViewById(R.id.eighteenRadioButton);
                break;
            case 24:
                radioButton = findViewById(R.id.twentyFourRadioButton);
                break;
            case 32:
                radioButton = findViewById(R.id.thirtyTwoRadioButton);
                break;
        }
        radioButton.setChecked(true);
    }
}
