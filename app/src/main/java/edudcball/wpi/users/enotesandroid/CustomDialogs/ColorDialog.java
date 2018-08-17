package edudcball.wpi.users.enotesandroid.CustomDialogs;

import android.app.Activity;
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

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import edudcball.wpi.users.enotesandroid.EventHandler;
import edudcball.wpi.users.enotesandroid.Note;
import edudcball.wpi.users.enotesandroid.R;

public class ColorDialog extends Dialog {

    private RadioGroup radioGroup;
    private static Map<Integer, RadioButton> buttonMap = new HashMap<>();

    private Button applyButton;
    private Button cancelButton;

    private Note note;
    private Context context;
    private EventHandler<Integer> confirmEvent;

    public ColorDialog(Context context, Note note, EventHandler<Integer> confirmEvent) {
        super(context);

        this.note = note;
        this.context = context;
        this.confirmEvent = confirmEvent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_color);

        applyButton = findViewById(R.id.applyButton);
        cancelButton = findViewById(R.id.cancelButton);

        radioGroup = findViewById(R.id.colorRadioButtonGroup);

        buttonMap = new HashMap<>();
        buttonMap.put(ContextCompat.getColor(context, R.color.noteYellow), (RadioButton)findViewById(R.id.yellowButton));
        buttonMap.put(ContextCompat.getColor(context, R.color.noteOrange), (RadioButton)findViewById(R.id.orangeButton));
        buttonMap.put(ContextCompat.getColor(context, R.color.noteRed), (RadioButton)findViewById(R.id.redButton));
        buttonMap.put(ContextCompat.getColor(context, R.color.noteGreen), (RadioButton)findViewById(R.id.greenButton));
        buttonMap.put(ContextCompat.getColor(context, R.color.noteBlue), (RadioButton)findViewById(R.id.blueButton));
        buttonMap.put(ContextCompat.getColor(context, R.color.notePurple), (RadioButton)findViewById(R.id.purpleButton));

        int selectedColor = 0;
        try {
            selectedColor = Color.parseColor(note.getColors().getString("body"));
        } catch (JSONException e) {
            selectedColor = ContextCompat.getColor(context, R.color.defaultNote);
        }

        if(buttonMap.containsKey(selectedColor))
            buttonMap.get(selectedColor).setChecked(true);

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int selectedID = radioGroup.getCheckedRadioButtonId();
                for(Map.Entry<Integer, RadioButton> entry: buttonMap.entrySet()){
                    if(entry.getValue().getId() == selectedID){
                        note.setColor(entry.getKey());
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
}
