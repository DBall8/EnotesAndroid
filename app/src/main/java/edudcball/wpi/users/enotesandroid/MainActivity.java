package edudcball.wpi.users.enotesandroid;

import android.app.Activity;
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

import edudcball.wpi.users.enotesandroid.AsyncTasks.LogoutTask;


/**
 * Class for creating the main view of the app, which is the list of notes
 */
public class MainActivity extends AppCompatActivity {

    private ListView notesList; // the listview listing each note by its title

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        notesList = (ListView) findViewById(R.id.NotesList);
        NoteManager.init(this, notesList, this.getApplicationContext());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.newNoteFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NoteManager.newNote();
            }
        });

        ColorConversions.init(this.getApplicationContext());
    }

    @Override
    public void onResume(){
        super.onResume();

        SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
        String session = sp.getString("session", null);
        if(session != null){
            NoteManager.resetCookies();
            NoteManager.addCookies(session);
        }

        NoteManager.retrieveNotes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if(id == R.id.action_logout){

            final Activity activity = this;
            new LogoutTask(){

                @Override
                protected void onPostExecute(String result) {
                    if(result == null) return;
                    activity.startActivity(new Intent(activity, LoginActivity.class));
                }
            }.execute();
        }

        return super.onOptionsItemSelected(item);
    }

}
