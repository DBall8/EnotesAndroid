package edudcball.wpi.users.enotesandroid;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.CustomDialogs.ColorDialog;
import edudcball.wpi.users.enotesandroid.CustomDialogs.FontDialog;
import edudcball.wpi.users.enotesandroid.CustomDialogs.FontSizeDialog;

/**
 * Activity for displaying a single note
 */

public class NoteActivity extends AppCompatActivity {

    // Views
    private Button saveButton;
    private Button cancelButton;
    private Button deleteButton;
    private EditText contentView;
    private Toolbar noteToolbar;
    private EditText titleBar;

    private Note note; // the note that this activity is currently displaying
    private int color; // the color of this note as a color ID

    private AlertDialog.Builder confirmDialog; // builder for a delete confirmation dialog

    /**
     * Called the first time the activtiy is created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // load layout
        setContentView(R.layout.activity_note);

        // hide the keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // get the tag from the calling activity
        String tag = getIntent().getStringExtra("Tag");
        note = NoteManager.getNote(tag);

        // find all the views
        noteToolbar = findViewById(R.id.noteToolbar);
        setSupportActionBar(noteToolbar);
        contentView = findViewById(R.id.noteText);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        deleteButton = findViewById(R.id.deleteButton);
        titleBar = findViewById(R.id.titleBar);

        // load the title to the title bar
        titleBar.setText(note.getTitle());

        // load the note content and set its font
        contentView.setText(note.getContent());
        contentView.requestFocus();
        setFontSize(note.getFontSize());
        setFont(note.getFont());

        // Load the note's color
        try{
            color = Color.parseColor(note.getColors().getString("body"));
        }
        catch(Exception e){
            color = getResources().getColor(R.color.defaultNote);
            Log.d("MYAPP", "Could not get color");
        }

        setColors();

        final Activity activity = this;

        // Set the save button to save changes
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            // update text
            note.setContent(contentView.getText().toString());
            note.setTitle(titleBar.getText().toString());

            NoteManager.updateNote(note, new EventHandler<String>() {
                @Override
                public void handle(String event) {

                    if(event == null){
                        NoteManager.sessionExpired(activity, "Connection to server lost, please login again.");
                        return;
                    }

                    try {
                        JSONObject obj = new JSONObject(event);
                        if (obj.getBoolean("sessionExpired")) {
                            Log.d("MYAPP", "Session expired");
                            NoteManager.sessionExpired(activity,"Session expired. Please log in again.");
                            return;
                        }
                        finish();
                    } catch (Exception e) {
                        Log.d("MYAPP", "Unable to form response JSON for update notes");
                        NoteManager.sessionExpired(activity, "Error when contacting server. Please try again later.");
                    }
                }
            });
            }
        });

        // set the cancel button to close without saving changes
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Create a dialog for confirming that the user wants to delete a note
        confirmDialog = new AlertDialog.Builder(this);
        confirmDialog.setTitle("Delete");
        confirmDialog.setMessage("Are you sure you want to delete this note?");
        confirmDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
            NoteManager.deleteNote(note.getTag(), new EventHandler<String>() {
                @Override
                public void handle(String event) {

                    if(event == null){
                        NoteManager.sessionExpired(activity, "Connection to server lost, please login again.");
                        return;
                    }

                    try{
                        JSONObject obj = new JSONObject(event);
                        if(obj.getBoolean("sessionExpired")){
                            Log.d("MYAPP", "Session expired");
                            NoteManager.sessionExpired(activity, "Session expired. Please log in again.");
                            return;
                        }
                        finish();
                    }
                    catch(Exception e){
                        Log.d("MYAPP", "Unable to form response JSON for delete note");
                        NoteManager.sessionExpired(activity, "Error when contacting server. Please try again later.");
                    }
                }
            });

            }
        });
        confirmDialog.setNegativeButton(android.R.string.no, null);

        // set the delete button to call the confirm dialog
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                confirmDialog.show();
            }
        });

    }

    /**
     * Sets the colors of all views that are set to match the note's color
     */
    private void setColors(){
        saveButton.setBackgroundColor(color);
        cancelButton.setBackgroundColor(color);
        deleteButton.setBackgroundColor(color);
        noteToolbar.setBackgroundColor(color);
    }

    /**
     * Override function that makes the menu open
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_note, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handles the selection of a menu item
     * @param item the menu item that is selected
     * @return true
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            // Opens color menu
            case R.id.colorMenuItem:
                // get the selected color ID from the note
                int selectedColor = 0;
                try {
                    selectedColor = Color.parseColor(note.getColors().getString("body"));
                } catch (JSONException e) {
                    selectedColor = ContextCompat.getColor(this, R.color.defaultNote);
                }
                // open the dialog, and have it update the note and colors upon completion
                ColorDialog cDialog = new ColorDialog(NoteActivity.this, selectedColor, new EventHandler<Integer>() {
                    @Override
                    public void handle(Integer eventColor) {
                        note.setColor(eventColor);
                        color = eventColor;
                        setColors();
                    }
                });
                cDialog.show();
                return true;

            // Opens the font menu
            case R.id.fontMenuItem:
                FontDialog fDialog = new FontDialog(NoteActivity.this, note.getFont(), new EventHandler<String>() {
                    @Override
                    public void handle(String stringEvent) {
                        note.setFont(stringEvent);
                        setFont(stringEvent);
                    }
                });
                fDialog.show();
                return true;

            // Opens the font size menu
            case R.id.fontSizeMenuItem:
                FontSizeDialog fsDialog = new FontSizeDialog(NoteActivity.this, note.getFontSize(), new EventHandler<Integer>() {
                    @Override
                    public void handle(Integer sizeEvent) {
                        note.setFontSize(sizeEvent);
                        setFontSize(sizeEvent);
                    }
                });
                fsDialog.show();
                return true;
            default:
                return true;
        }
    }

    /**
     * Set the font on the screen
     * @param font the string version of the font to set the screen to
     */
    private void setFont(String font){
        try {
            // Attempt to load the font data
            Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/" + font + ".ttf");
            contentView.setTypeface(typeface);
            titleBar.setTypeface(typeface);
        }
        catch(Exception e){
            // If the font data didnt exist, switch to default
            Log.d("MYAPP", "FONT: " + font + " is not loaded.");
            contentView.setTypeface(Typeface.DEFAULT);
        }
    }

    /**
     * Sets the font sizez of the note
     * @param size the size tos et to
     */
    private void setFontSize(int size){
        contentView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 4);
    }
}