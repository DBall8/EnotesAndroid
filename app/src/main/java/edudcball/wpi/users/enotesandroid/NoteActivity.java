package edudcball.wpi.users.enotesandroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;

import AsyncTasks.DeleteTask;
import AsyncTasks.UpdateNoteTask;

/**
 * Created by Owner on 1/6/2018.
 */

public class NoteActivity extends AppCompatActivity {

    private Button saveButton;
    private Button cancelButton;
    private Button deleteButton;
    private EditText contentView;
    private ConstraintLayout layout;
    private String tag;

    private AlertDialog.Builder confirmDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        tag = getIntent().getStringExtra("Tag");
        String content = getIntent().getStringExtra("Content");
        contentView = (EditText) findViewById(R.id.noteText);
        saveButton = (Button) findViewById(R.id.saveButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        deleteButton = (Button) findViewById(R.id.deleteButton);
        layout = (ConstraintLayout) findViewById(R.id.noteConstraint);
        contentView.setText(content);

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
            color = Color.parseColor(NoteManager.getNote(tag).getColors().getString("body"));
        }
        catch(Exception e){
            color = getResources().getColor(R.color.defaultNote);
            Log.d("MYAPP", "Could not get color");
        }
        saveButton.setBackgroundColor(color);
        cancelButton.setBackgroundColor(color);
        deleteButton.setBackgroundColor(color);
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

        contentView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(final Editable editable) {

            }
        });

    }
}
