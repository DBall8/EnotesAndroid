package edudcball.wpi.users.enotesandroid;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toolbar;

/**
 * Created by Owner on 1/6/2018.
 */

public class NoteActivity extends AppCompatActivity {

    private Button saveButton;
    private Button cancelButton;
    private Button deleteButton;
    private EditText contentView;
    private ConstraintLayout layout;
    private TextView titleBar;
    private String tag;

    private AlertDialog.Builder confirmDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        tag = getIntent().getStringExtra("Tag");
        Note n = NoteManager.getNote(tag);

        contentView = findViewById(R.id.noteText);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        deleteButton = findViewById(R.id.deleteButton);
        layout = findViewById(R.id.noteConstraint);
        titleBar = findViewById(R.id.titleBar);

        titleBar.setText(n.getTitle());
        contentView.setText(n.getContent());

        final NoteActivity me = this;
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Note n = NoteManager.getNote(tag);
                n.setContent(contentView.getText().toString());
                NoteManager.updateNote(tag);
                finish();
            }
        });

        int color;
        try{
            color = Color.parseColor(n.getColors().getString("body"));
        }
        catch(Exception e){
            color = getResources().getColor(R.color.defaultNote);
            Log.d("MYAPP", "Could not get color");
        }
        saveButton.setBackgroundColor(color);
        cancelButton.setBackgroundColor(color);
        deleteButton.setBackgroundColor(color);
        titleBar.setBackgroundColor(color);
        //layout.setBackgroundColor(color);


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        confirmDialog = new AlertDialog.Builder(this);
        confirmDialog.setTitle("Delete");
        confirmDialog.setMessage("Are you sure you want to delete this note?");
        confirmDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                NoteManager.deleteNote(tag);
                finish();
            }
        });
        confirmDialog.setNegativeButton(android.R.string.no, null);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                me.confirmDialog.show();
            }
        });



    }
}
