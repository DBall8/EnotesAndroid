package edudcball.wpi.users.enotesandroid.Old.CustomDialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.Map;

import edudcball.wpi.users.enotesandroid.EventHandler;
import edudcball.wpi.users.enotesandroid.R;

/**
 * An abstract class for creating simple menu dialogs for selecting from one radio button group
 * @param <T> the type of data to associate with each radio button
 */
public class CustomOptionsDialog<T> extends Dialog {

    private int layoutID; // the ID of the layout file to use for the dialog

    protected RadioGroup radioGroup; // the radio button group. ID MUST BE radioButtonGroup
    protected Map<T, RadioButton> buttonMap; // a map for mapping radio buttons to values

    private Button applyButton; // button for saving the new selected option
    private Button cancelButton; // button for closing the dialog without saving the new selection

    protected T selectedItem; // the item to be selected at the start
    protected Context context; // the context the dialog is opened in
    protected EventHandler<T> confirmEvent; // the event to call when the applyButton is pressed

    /**
     * Constructor
     * @params see above
     */
    public CustomOptionsDialog(Context context, int layoutID, T selectedItem, EventHandler<T> confirmEvent) {
        super(context);

        this.layoutID = layoutID;
        this.selectedItem = selectedItem;
        this.context = context;
        this.confirmEvent = confirmEvent;
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
        setContentView(layoutID);

        applyButton = findViewById(R.id.applyButton);
        cancelButton = findViewById(R.id.cancelButton);

        radioGroup = findViewById(R.id.radioButtonGroup);

        // set apply button action
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get selected radio button
                int selectedID = radioGroup.getCheckedRadioButtonId();
                for(Map.Entry<T, RadioButton> entry: buttonMap.entrySet()){
                    if(entry.getValue().getId() == selectedID){

                        // call confirm event with the value mapped to the selected radio button
                        confirmEvent.handle(entry.getKey());
                        break;
                    }
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

    /**
     * Select the option set to be selected on start
     */
    protected void selectOption(){
        if(buttonMap.containsKey(selectedItem))
            buttonMap.get(selectedItem).setChecked(true);
    }
}
