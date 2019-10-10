package edudcball.wpi.users.enotesandroid.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import edudcball.wpi.users.enotesandroid.Callback;
import edudcball.wpi.users.enotesandroid.EventHandler;
import edudcball.wpi.users.enotesandroid.CustomDialogs.SettingsDialog;
import edudcball.wpi.users.enotesandroid.data.classes.Page;
import edudcball.wpi.users.enotesandroid.noteDataTypes.NoteLookupTable;
import edudcball.wpi.users.enotesandroid.R;
import edudcball.wpi.users.enotesandroid.Settings;
import edudcball.wpi.users.enotesandroid.data.PageManager;
import edudcball.wpi.users.enotesandroid.data.UserManager;
import edudcball.wpi.users.enotesandroid.observerPattern.IObserver;


/**
 * Class for creating the main view of the app, which is the list of notes
 */
public class MainActivity extends EnotesActivity implements IObserver {

    PageManager pages;
    private ArrayAdapter<String> pageAdapter;
    private ListView pageList;
    private Menu menu;

    /**
     * Gets called the first time the main activity is created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMainActivity(this);

        pages = UserManager.getInstance().getPageManager();

        NoteLookupTable.init(this.getApplicationContext());
        Settings.init(this.getApplicationContext());

        // setup layout
        setContentView(R.layout.activity_main);

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.pageToolbar);
        setSupportActionBar(toolbar);

        // Initialize the static note handler with the notesList
        pageList = findViewById(R.id.PageList);
        pageAdapter = buildPageAdapter();
        pageList.setAdapter(pageAdapter);
        pages.subscribe(this);

        final MainActivity self = this;

        pageList.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                pages.selectPage(i);
                launchActivity(self, PageActivity.class);
            }
        });

        // Set up the floating button for adding notes
        FloatingActionButton fab = findViewById(R.id.newNoteFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pages.createPage(new Callback<Page>() {
                    @Override
                    public void run(Page param) {
                        if (param == null){
                            showErrorAndLogout("Error communicating with server.");
                        }
                        else{
                            pages.selectPage(param.getId());
                            launchActivity(getApplicationContext(), PageActivity.class);
                        }
                    }
                });
            }
        });

        UserManager.getInstance().getConnectionManager().startUpdateDaemon(this);
    }

    /**
     * Gets called each time the app returns to the main activity
     */
    @Override
    public void onResume(){
        super.onResume();

        View view = getCurrentFocus();
        if (view != null) view.clearFocus();

        if(!UserManager.getInstance().isUserSignedIn()){
            launchActivity(this, LoginActivity.class);
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
                        pageAdapter.notifyDataSetChanged();
                    }
                });
                settingsDialog.show();
                break;
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
                UserManager.getInstance().logOut(this.getApplicationContext(), new Callback<String>() {
                    @Override
                    public void run(String param) {
                        activity.launchActivity(activity, LoginActivity.class);
                    }
                });
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
        ArrayAdapter<String> pageAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, pages.getPageTitles()){
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

    public void update(String id){
        pageAdapter.notifyDataSetChanged();
    }
}
