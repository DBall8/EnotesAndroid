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
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
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

import edudcball.wpi.users.enotesandroid.Callback;
import edudcball.wpi.users.enotesandroid.CustomDialogs.SettingsDialog;
import edudcball.wpi.users.enotesandroid.EventHandler;
import edudcball.wpi.users.enotesandroid.R;
import edudcball.wpi.users.enotesandroid.Settings;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.userTasks.LogoutTask;
import edudcball.wpi.users.enotesandroid.data.SortedList;
import edudcball.wpi.users.enotesandroid.data.UserManager;
import edudcball.wpi.users.enotesandroid.data.classes.Note;
import edudcball.wpi.users.enotesandroid.data.classes.Page;
import edudcball.wpi.users.enotesandroid.observerPattern.IObserver;


/**
 * Class for creating the main view of the app, which is the list of notes
 */
public class PageActivity extends EnotesActivity implements IObserver {

    private Page page;
    private ArrayAdapter<String> noteAdapter;
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

        pageTitle = findViewById(R.id.pageTitleBar);
        pageTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            if(!hasFocus){
                saveTitle();
            }
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
                page.createNote(new Callback<Note>() {
                    @Override
                    public void run(Note note) {
                        if (note == null){
                            showErrorAndLogout("Error communicating with server.");
                        }
                        else {
                            page.selectNote(note.getId());
                            launchActivity(getApplicationContext(), NoteActivity.class);
                        }
                    }
                });
            }
        });

        // Create a dialog for confirming that the user wants to delete a note
        confirmDialog = new AlertDialog.Builder(this);
        confirmDialog.setTitle("Delete Page");
        confirmDialog.setMessage("Are you sure you want to delete this page?\nAll notes in this page will be deleted and cannot be recovered.");
        confirmDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                UserManager.getInstance().getPageManager().deletePage(page.getId(), new Callback<Boolean>() {
                    @Override
                    public void run(Boolean successful) {
                        if(successful){
                            finish();
                        }
                        else{
                            showErrorAndLogout("Problem communicating with server.");
                        }
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

        page = UserManager.getInstance().getPageManager().getActivePage();

        // Clear focus, unless the title is empty
        if (page.getDisplayTitle().length() > 0){
            View view = getCurrentFocus();
            if (view != null) view.clearFocus();
        }
        else{
            pageTitle.requestFocus();
        }

        if(page == null){
            finish();
        }

        // Initialize the static note handler with the notesList
        ListView notesList = findViewById(R.id.NotesList);
        noteAdapter = buildNotesAdapter();
        notesList.setAdapter(noteAdapter);

        page.subscribeToNoteList(this);

        notesList.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            saveTitle();
            page.selectNote(i);
            launchActivity(getApplicationContext(), NoteActivity.class);
            }
        });

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
        switch(page.getSortMode()){
            case COLOR:
                sortByMenu.findItem(R.id.action_color).setChecked(true);
                break;
            case LATEST:
            default:
                sortByMenu.findItem(R.id.action_recent).setChecked(true);
                break;
            case ALPHA:
                sortByMenu.findItem(R.id.action_alpha).setChecked(true);
                break;
        }

        // Turn the Delete Page item red
        MenuItem deletePageItem = menu.findItem(R.id.action_deletePage);
        SpannableString s = new SpannableString(deletePageItem.getTitle());
        s.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.deleteRed)), 0, s.length(), 0);
        deletePageItem.setTitle(s);

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
                page.setSortMode(SortedList.SortMode.COLOR);
                clearMenuSelection(menu.findItem(R.id.action_sortBy).getSubMenu());
                item.setChecked(true);
                return true;
            case R.id.action_recent:
                page.setSortMode(SortedList.SortMode.LATEST);
                clearMenuSelection(menu.findItem(R.id.action_sortBy).getSubMenu());
                item.setChecked(true);
                return true;
            case R.id.action_alpha:
                page.setSortMode(SortedList.SortMode.ALPHA);
                clearMenuSelection(menu.findItem(R.id.action_sortBy).getSubMenu());
                item.setChecked(true);
                return true;
            case R.id.action_deletePage:
                confirmDialog.show();
                break;
            case R.id.action_settings:
                SettingsDialog settingsDialog = new SettingsDialog(PageActivity.this, new EventHandler<Void>() {
                    @Override
                    public void handle(Void event) {
                        // TODO re-implement
//                        updateNoteAdapter();
                    }
                });
                settingsDialog.show();
                return true;
            case R.id.action_help:
                launchActivity(this.getApplicationContext(), HelpActivity.class);
                break;
            case R.id.action_password:
                launchActivity(this.getApplicationContext(), ChangePasswordActivity.class);
                break;
            // Logout the user
            case R.id.action_logout:
                final EnotesActivity activity = this;

                // create a background task that logs out the user
                new LogoutTask(new Callback<String>(){
                    @Override
                    public void run(String param){
                        if(param == null) return;
                        activity.launchActivity(activity, LoginActivity.class);
                    }
                }).execute();
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
        ArrayAdapter<String> noteAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, page.getNoteTitleList()){
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
                Note n = page.getNote(position);
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
        if(page == null ||
           page.getName().equals(pageTitle.getText().toString())) {
            return;
        }
        page.setName(pageTitle.getText().toString());
    }

    public void update(String id) {
        noteAdapter.notifyDataSetChanged();
    }
}
