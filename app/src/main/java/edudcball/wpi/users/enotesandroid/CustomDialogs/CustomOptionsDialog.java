package edudcball.wpi.users.enotesandroid.CustomDialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.Map;

import edudcball.wpi.users.enotesandroid.EventHandler;
import edudcball.wpi.users.enotesandroid.Note;
import edudcball.wpi.users.enotesandroid.R;

public class CustomOptionsDialog<T> extends Dialog {

    private int layoutID;

    protected RadioGroup radioGroup;
    protected Map<T, RadioButton> buttonMap;

    private Button applyButton;
    private Button cancelButton;

    protected T selectedItem;
    protected Context context;
    protected EventHandler<T> confirmEvent;

    public CustomOptionsDialog(Context context, int layoutID, T selectedItem, EventHandler<T> confirmEvent) {
        super(context);

        this.layoutID = layoutID;
        this.selectedItem = selectedItem;
        this.context = context;
        this.confirmEvent = confirmEvent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(layoutID);

        applyButton = findViewById(R.id.applyButton);
        cancelButton = findViewById(R.id.cancelButton);

        radioGroup = findViewById(R.id.radioButtonGroup);

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int selectedID = radioGroup.getCheckedRadioButtonId();
                for(Map.Entry<T, RadioButton> entry: buttonMap.entrySet()){
                    if(entry.getValue().getId() == selectedID){
                        confirmEvent.handle(entry.getKey());
                        break;
                    }
                }

                dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

    }

    protected void selectOption(){
        if(buttonMap.containsKey(selectedItem))
            buttonMap.get(selectedItem).setChecked(true);
    }
}
