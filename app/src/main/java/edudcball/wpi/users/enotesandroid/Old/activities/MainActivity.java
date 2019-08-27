package edudcball.wpi.users.enotesandroid.Old.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import edudcball.wpi.users.enotesandroid.Old.AsyncTasks.userTasks.LogoutTask;
import edudcball.wpi.users.enotesandroid.EventHandler;
import edudcball.wpi.users.enotesandroid.Old.NoteManager.NoteManager;
import edudcball.wpi.users.enotesandroid.R;
import edudcball.wpi.users.enotesandroid.Settings;
import edudcball.wpi.users.enotesandroid.Old.noteDataTypes.NoteLookupTable;
import edudcball.wpi.users.enotesandroid.Old.CustomDialogs.SettingsDialog;


/**
 * Class for creating the main view of the app, which is the list of notes
 */
public class MainActivity extends AppCompatActivity {

    private static final class SingletonHelper{
        private static ArrayAdapter<String> pageAdapter;
        private static boolean loaded = false;
    }
    private Menu menu;

    /**
     * Gets called the first time the main activity is created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NoteLookupTable.init(this.getApplicationContext());
        Settings.init(this.getApplicationContext());

        // setup layout
        setContentView(R.layout.activity_main);

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.pageToolbar);
        setSupportActionBar(toolbar);

        // Initialize the static note handler with the notesList
        ListView pageList = findViewById(R.id.PageList);
        SingletonHelper.pageAdapter = buildPageAdapter();
        pageList.setAdapter(SingletonHelper.pageAdapter);

        final MainActivity self = this;

        pageList.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                NoteManager.switchToPage(self, i);
            }
        });

        // Set up the floating button for adding notes
        FloatingActionButton fab = findViewById(R.id.newNoteFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NoteManager.newPage(self);
            }
        });
    }

    /**
     * Gets called each time the app returns to the main activity
     */
    @Override
    public void onResume(){
        super.onResume();
        if(!SingletonHelper.loaded){
            // look for a saved session in on the phone
            SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
            String session = sp.getString("session", null);

            // if a session is saved, load the session into the cookie manager and load the user's notes
            if(session != null){
                NoteManager.resetCookies();
                NoteManager.addCookies(session);
                NoteManager.retrieveNotes(this, new EventHandler<Void>() {
                    @Override
                    public void handle(Void event) {
                        SingletonHelper.loaded = true;
                    }
                });
            }
            // If no session is saved, move to login screen
            else{
                startActivity(new Intent(this, LoginActivity.class));
            }
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

        MenuItem sortByMenuItem = menu.findItem(R.id.action_sortBy);
        sortByMenuItem.setVisible(false);
        MenuItem deletePageItem = menu.findItem(R.id.action_deletePage);
        deletePageItem.setVisible(false);

        return true;
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
            case R.id.action_settings:
                SettingsDialog settingsDialog = new SettingsDialog(MainActivity.this, new EventHandler<Void>() {
                    @Override
                    public void handle(Void event) {
                        SingletonHelper.pageAdapter.notifyDataSetChanged();
                    }
                });
                settingsDialog.show();
                break;
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
    private ArrayAdapter<String> buildPageAdapter(){

        final Context context = this.getApplicationContext();
        ArrayAdapter<String> pageAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, NoteManager.getPageTitles()){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(ContextCompat.getColor(context, R.color.black));
                Drawable bg = ContextCompat.getDrawable(context, R.drawable.page_icon);

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

                return tv;
            }
        };

        return pageAdapter;
    }

    public static void unLoad(){
        SingletonHelper.loaded = false;
    }

    public static void notifyAdatperChanged(){
        if(SingletonHelper.pageAdapter != null)
            SingletonHelper.pageAdapter.notifyDataSetChanged();
    }
}
