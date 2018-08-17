package edudcball.wpi.users.enotesandroid;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import edudcball.wpi.users.enotesandroid.CustomDialogs.ColorDialog;

/**
 * Created by Owner on 1/6/2018.
 */

public class NoteActivity extends AppCompatActivity {

    private Button saveButton;
    private Button cancelButton;
    private Button deleteButton;
    private EditText contentView;
    private ConstraintLayout layout;
    private Toolbar noteToolbar;
    private EditText titleBar;

    private Note note;
    private int color;

    private AlertDialog.Builder confirmDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        String tag = getIntent().getStringExtra("Tag");
        note = NoteManager.getNote(tag);

        noteToolbar = findViewById(R.id.noteToolbar);
        setSupportActionBar(noteToolbar);
        contentView = findViewById(R.id.noteText);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        deleteButton = findViewById(R.id.deleteButton);
        layout = findViewById(R.id.noteConstraint);
        titleBar = findViewById(R.id.titleBar);

        titleBar.setText(note.getTitle());

        contentView.setText(note.getContent());
        contentView.requestFocus();

        final NoteActivity me = this;
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                note.setContent(contentView.getText().toString());
                note.setTitle(titleBar.getText().toString());
                NoteManager.updateNote(note.getTag());
                finish();
            }
        });

        try{
            color = Color.parseColor(note.getColors().getString("body"));
        }
        catch(Exception e){
            color = getResources().getColor(R.color.defaultNote);
            Log.d("MYAPP", "Could not get color");
        }

        setColors();

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
                NoteManager.deleteNote(note.getTag());
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

    private void setColors(){
        saveButton.setBackgroundColor(color);
        cancelButton.setBackgroundColor(color);
        deleteButton.setBackgroundColor(color);
        noteToolbar.setBackgroundColor(color);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_note, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.colorMenuItem:
                ColorDialog dialog = new ColorDialog(NoteActivity.this, note, new EventHandler<Integer>() {
                    @Override
                    public void handle(Integer event) {
                        color = event;
                        setColors();
                    }
                });
                dialog.show();
                return true;
            default:
                return true;
        }
    }
}
