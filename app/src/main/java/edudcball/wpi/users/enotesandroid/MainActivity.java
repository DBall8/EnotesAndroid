package edudcball.wpi.users.enotesandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.AsyncTasks.LogoutTask;
import edudcball.wpi.users.enotesandroid.CustomDialogs.SettingsDialog;


/**
 * Class for creating the main view of the app, which is the list of notes
 */
public class MainActivity extends AppCompatActivity {

    private ListView notesList; // the listview listing each note by its title
    private Menu menu;

    /**
     * Gets called the first time the main activity is created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setup layout
        setContentView(R.layout.activity_main);

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize the static note handler with the notesList
        notesList = findViewById(R.id.NotesList);
        NoteManager.init(this, notesList, this.getApplicationContext());

        // Set up the floating button for adding notes
        FloatingActionButton fab = findViewById(R.id.newNoteFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NoteManager.newNote();
            }
        });

        // initialize the color conversion class
        ColorConversions.init(this.getApplicationContext());
    }

    /**
     * Gets called each time the app returns to the main activity
     */
    @Override
    public void onResume(){
        super.onResume();

        // look for a saved session in on the phone
        SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
        String session = sp.getString("session", null);

        // if a session is saved, load the session into the cookie manager and load the user's notes
        if(session != null){
            NoteManager.resetCookies();
            NoteManager.addCookies(session);
            NoteManager.retrieveNotes();
        }
        // If no session is saved, move to login screen
        else{
            startActivity(new Intent(this, LoginActivity.class));
        }


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
            case R.id.action_settings:
                SettingsDialog settingsDialog = new SettingsDialog(MainActivity.this);
                settingsDialog.show();
                return true;
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

}
