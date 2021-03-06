package edudcball.wpi.users.enotesandroid.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.AsyncTasks.userTasks.LogoutTask;
import edudcball.wpi.users.enotesandroid.CustomDialogs.SettingsDialog;
import edudcball.wpi.users.enotesandroid.EventHandler;
import edudcball.wpi.users.enotesandroid.NoteManager.NoteManager;
import edudcball.wpi.users.enotesandroid.R;
import edudcball.wpi.users.enotesandroid.Settings;
import edudcball.wpi.users.enotesandroid.objects.Note;
import edudcball.wpi.users.enotesandroid.objects.NotePage;


/**
 * Class for creating the main view of the app, which is the list of notes
 */
public class NotePageActivity extends AppCompatActivity {

    private NotePage page;
    private static final class SingletonHelper{
        private static ArrayAdapter<String> noteAdapter;
    }
    private Menu menu;
    private AlertDialog.Builder confirmDialog;
    EditText pageTitle;

    /**
     * Gets called the first time the main activity is created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setup layout
        setContentView(R.layout.activity_note_page);

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.pageNameToolbar);
        setSupportActionBar(toolbar);

        final Activity activity = this;

        pageTitle = findViewById(R.id.pageTitleBar);
        pageTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    saveTitle();
                }
            }
        });

        // Initialize the static note handler with the notesList
        ListView notesList = findViewById(R.id.NotesList);
        SingletonHelper.noteAdapter = buildNotesAdapter();
        notesList.setAdapter(SingletonHelper.noteAdapter);

        notesList.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                saveTitle();
                NoteManager.switchToNote(activity, i);
            }
        });

        // Set up back button
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveTitle();
                finish();
            }
        });

        // Set up the floating button for adding notes
        FloatingActionButton fab = findViewById(R.id.newNoteFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveTitle();
                NoteManager.newNote(activity);
            }
        });

        // Create a dialog for confirming that the user wants to delete a note
        confirmDialog = new AlertDialog.Builder(this);
        confirmDialog.setTitle("Delete Page");
        confirmDialog.setMessage("Are you sure you want to delete this page?\nAll notes in this page will be deleted and cannot be recovered.");
        confirmDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                NoteManager.deletePage(activity, page.getPageID(), new EventHandler<String>() {
                    @Override
                    public void handle(String result) {
                        finish();
                    }
                });
            }
        });
        confirmDialog.setNegativeButton(android.R.string.no, null);
    }

    /**
     * Gets called each time the app returns to the main activity
     */
    @Override
    public void onResume(){
        super.onResume();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        String pageID = getIntent().getStringExtra("pageID");
        page = NoteManager.getPage(pageID);

        if(page == null){
            finish();
        }

        if(getIntent().getBooleanExtra("new", false)){
            pageTitle.requestFocus();
        }
        pageTitle.setText(page.getName());


    }

    /**
     * Simply sets up the toolbar menu to open the main activity menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        Menu sortByMenu = menu.findItem(R.id.action_sortBy).getSubMenu();
        switch(Settings.getSortBy()){
            case COLOR:
                sortByMenu.findItem(R.id.action_color).setChecked(true);
                break;
            case RECENT:
            default:
                sortByMenu.findItem(R.id.action_recent).setChecked(true);
                break;
            case ALPHA:
                sortByMenu.findItem(R.id.action_alpha).setChecked(true);
                break;
        }

        return true;
    }

    private void clearMenuSelection(Menu menu){
        for(int i=0; i<menu.size(); i++){
            menu.getItem(i).setChecked(false);
        }
    }

    /**
     * Handles the selection of a certain menu item
     * @param item the menu item that was selected
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Get the id of the menu item that was selected
        switch(item.getItemId()){
            case R.id.action_color:
                Settings.setSortBy(Settings.SortBy.COLOR);
                clearMenuSelection(menu.findItem(R.id.action_sortBy).getSubMenu());
                item.setChecked(true);
                return true;
            case R.id.action_recent:
                Settings.setSortBy(Settings.SortBy.RECENT);
                clearMenuSelection(menu.findItem(R.id.action_sortBy).getSubMenu());
                item.setChecked(true);
                return true;
            case R.id.action_alpha:
                Settings.setSortBy(Settings.SortBy.ALPHA);
                clearMenuSelection(menu.findItem(R.id.action_sortBy).getSubMenu());
                item.setChecked(true);
                return true;
            case R.id.action_deletePage:
                confirmDialog.show();
                break;
            case R.id.action_settings:
                SettingsDialog settingsDialog = new SettingsDialog(NotePageActivity.this, new EventHandler<Void>() {
                    @Override
                    public void handle(Void event) {
                        updateNoteAdapter();
                    }
                });
                settingsDialog.show();
                return true;
            case R.id.action_help:
                startActivity(new Intent(this.getApplicationContext(), HelpActivity.class));
                break;
            case R.id.action_password:
                startActivity(new Intent(this.getApplicationContext(), ChangePasswordActivity.class));
                break;
            // Logout the user
            case R.id.action_logout:
                final Activity activity = this;

                // create a background task that logs out the user
                new LogoutTask(){

                    @Override
                    protected void onPostExecute(String result) {
                        if(result == null) return;
                        activity.startActivity(new Intent(activity, LoginActivity.class));
                    }
                }.execute();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Builds the array adapter for handling the list of notes on the main screen
     * @return an array adapter
     */
    private ArrayAdapter<String> buildNotesAdapter(){

        final Context context = this.getApplicationContext();
        ArrayAdapter<String> noteAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, NoteManager.getNoteTitles()){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(ContextCompat.getColor(context, R.color.black));
                GradientDrawable bg = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.note_icon);

                tv.setBackground(bg);

                switch(Settings.getIconSize()){
                    case SMALL:
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, parent.getResources().getInteger(R.integer.font_small));
                        break;
                    case MEDIUM:
                    default:
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, parent.getResources().getInteger(R.integer.font_medium));
                        break;
                    case LARGE:
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, parent.getResources().getInteger(R.integer.font_large));
                        break;
                }

                // Get the note by its position in the list
                Note n = NoteManager.getNote(position);
                // Set the background to match the note's color
                JSONObject colors = n.getColors();

                if(colors != null){
                    try {
                        bg.setColor(Color.parseColor(colors.getString("body")));
                        //bg.setColorFilter(Color.parseColor(colors.getString("body")), PorterDuff.Mode.OVERLAY);
                        //tv.setBackgroundColor(Color.parseColor(colors.getString("body")));
                    }catch(Exception e){
                        tv.setBackgroundColor(getResources().getColor(R.color.defaultNote));
                    }
                }
                else{
                    tv.setBackgroundColor(getResources().getColor(R.color.defaultNote));
                }


                return tv;
            }
        };

        return noteAdapter;
    }

    private void saveTitle(){
        if(page == null || page.getName().equals(pageTitle.getText().toString())) return;
        final Activity activity = this;
        page.setName(pageTitle.getText().toString());
        NoteManager.updatePage(activity, page, null);
    }

    public static void updateNoteAdapter(){
        if(SingletonHelper.noteAdapter != null)
            SingletonHelper.noteAdapter.notifyDataSetChanged(); }
}
