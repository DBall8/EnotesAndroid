package edudcball.wpi.users.enotesandroid;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.CookieManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edudcball.wpi.users.enotesandroid.AsyncTasks.DeleteTask;
import edudcball.wpi.users.enotesandroid.AsyncTasks.NewNoteTask;
import edudcball.wpi.users.enotesandroid.AsyncTasks.RetrieveNotesTask;
import edudcball.wpi.users.enotesandroid.AsyncTasks.UpdateNoteTask;

/**
 * Created by Owner on 1/5/2018.
 */

public class NoteManager {

    private MainActivity parent;

    private HashMap<String, Note> notes = new HashMap<String, Note>();
    private ArrayList<String> noteTagLookup = new ArrayList<String>();
    private ArrayList<String> noteTitles = new ArrayList<String>();

    private ArrayAdapter<String> noteAdapter;
    private static String username = "";

    public static CookieManager cookies = new CookieManager();

    public static void init(final MainActivity parent, ListView lv, final Context context){

        NoteManager instance = getInstance();

        // save parent for screen switching
        instance.parent = parent;

        // Load the noteAdapter used for displaying icons represeting notes that can be clicked on
        instance.noteAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, instance.noteTitles){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(ContextCompat.getColor(context, R.color.black));

                // Get the note by its position in the list
                Note n = getNote(getInstance().noteTagLookup.get(position));
                // Set the background to match the note's color
                JSONObject colors = n.getColors();
                if(colors != null){
                    try {
                        tv.setBackgroundColor(Color.parseColor(colors.getString("body")));
                    }catch(Exception e){
                        tv.setBackgroundColor(parent.getResources().getColor(R.color.defaultNote));
                    }
                }
                else{
                    tv.setBackgroundColor(parent.getResources().getColor(R.color.defaultNote));
                }

                return tv;
            }
        };
        // Load the adapter for the listview
        lv.setAdapter(instance.noteAdapter);
        // Set clicking on an item to open that note
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Note n = getNote(getInstance().noteTagLookup.get(i));
                switchToNote(n);
            }
        });
    }

    /**
     * Retrieve all notes for the user from the server and then load them
     */
    public static void retrieveNotes(){
        new RetrieveNotesTask(){

            @Override
            protected void onPostExecute(String result) {
                getInstance().loadNotes(result);
            }
        }.execute();
    }

    /**
     * Load all notes for the user onto the screen
     * @param res the string version of an http response from the server
     */
    private void loadNotes(String res){
        try{
            JSONObject obj = new JSONObject(res);
            if(obj.getBoolean("sessionExpired")){
                Log.d("MYAPP", "Session expired");
                sessionExpired("Session expired. Please log in again.");
                return;
            }

            // Clear everything, start from empty, except the notes list to preserve their z index
            noteTitles.clear();
            noteTagLookup.clear();

            // store the user's username
            username = obj.getString("username");

            // Load each note in the http response
            JSONArray arr = obj.getJSONArray("notes");
            for(int i=0; i<arr.length(); i++){
                JSONObject noteJSON = arr.getJSONObject(i);
                String tag = noteJSON.getString("tag");

                Note n = new Note(noteJSON);
                // If the note already existed, preserve its zindex
                if(notes.containsKey(tag)){
                    int z = notes.get(tag).getZ();
                    notes.remove(tag);
                    n.setZIndex(z);
                }
                notes.put(n.getTag(), n);

            }
            createNoteList();
            Log.d("MYAPP", "NOTES LOADED");
        }
        catch(Exception e){
            Log.d("MYAPP", "Unable to form response JSON for get notes");
            Log.d("MYAPP", e.getMessage().toString());
            sessionExpired("Error when contacting server. Please try again later.");
        }

    }

    /**
     * Redirects the user back to the login screen
     * @param message the message to display once back on the login screen
     */
    public static void sessionExpired(String message){
        Intent next = new Intent(getInstance().parent, LoginActivity.class);
        next.putExtra("error", message);
        getInstance().parent.startActivity(next);
    }


    /**
     * Builds the list of notes from the notes hashmap, adding them in order
     */
    private void createNoteList(){

        ArrayList<String> workingList = new ArrayList<String>();
        for(Map.Entry<String, Note> cursor: getInstance().notes.entrySet()){
            workingList.add(cursor.getKey());
        }

        int len = workingList.size();

        int z;
        // Loop through and add the note with the highest z index until all notes are added
        for(int i=0; i<len; i++){
            String maxTag = workingList.get(0);
            int maxZ = getNote(workingList.get(0)).getZ();
            for(int j=1; j<workingList.size(); j++){
                if((z = getNote(workingList.get(j)).getZ()) > maxZ){
                    maxTag = workingList.get(j);
                    maxZ = z;
                }
            }
            // Add the largest zindex note to the titles list and remove it from the workingList
            if(maxTag != null){
                workingList.remove(maxTag);
                Note note = getNote(maxTag);
                getInstance().noteTitles.add(getTitleFromNote(note));
                getInstance().noteTagLookup.add(maxTag);
            }

        }

        // Notify the adatper that the list has been changed
        getInstance().noteAdapter.notifyDataSetChanged();
    }


    public static void addNote(Note n){
        getInstance().notes.put(n.getTag(), n);
        getInstance().noteTagLookup.add(n.getTag());
        getInstance().noteTitles.add(getTitleFromNote(n));
        getInstance().noteAdapter.notifyDataSetChanged();
    }

    public static void newNote(){
        if(username == null){
            Log.d("MYAPP", "Username is null");
        }
        Note n = new Note();

        new NewNoteTask(n){

            @Override
            protected void onPostExecute(String result) {
                try{
                    JSONObject obj = new JSONObject(result);
                    if(obj.getBoolean("sessionExpired")){
                        Log.d("MYAPP", "Session expired");
                        sessionExpired("Session expired. Please log in again.");
                        return;
                    }
                }
                catch(Exception e){
                    Log.d("MYAPP", "Unable to form response JSON for update notes");
                    sessionExpired("Error when contacting server. Please try again later.");
                }
            }
        }.execute();

        addNote(n);
        decrementZ(n.getTag());
        switchToNote(n);
    }

    public static void updateNote(String tag){
        Note n = getNote(tag);
        new UpdateNoteTask(n){

            @Override
            protected void onPostExecute(String result) {
                try{
                    JSONObject obj = new JSONObject(result);
                    if(obj.getBoolean("sessionExpired")){
                        Log.d("MYAPP", "Session expired");
                        sessionExpired("Session expired. Please log in again.");
                        return;
                    }
                }
                catch(Exception e){
                    Log.d("MYAPP", "Unable to form response JSON for update notes");
                    sessionExpired("Error when contacting server. Please try again later.");
                }
            }
        }.execute();
    }

    public static void deleteNote(String tag){
        getInstance().notes.remove(tag);
        for(int i=0; i< getInstance().noteTagLookup.size(); i++){
            if(getInstance().noteTagLookup.get(i).equals(tag)){
                getInstance().noteTagLookup.remove(i);
                getInstance().noteTitles.remove(i);
                getInstance().noteAdapter.notifyDataSetChanged();
                i--;
            }
        }
        new DeleteTask(tag){
            @Override
            protected void onPostExecute(String result) {
                try{
                    JSONObject obj = new JSONObject(result);
                    if(obj.getBoolean("sessionExpired")){
                        Log.d("MYAPP", "Session expired");
                        NoteManager.sessionExpired("Session expired. Please log in again.");
                        return;
                    }
                }
                catch(Exception e){
                    Log.d("MYAPP", "Unable to form response JSON for delete note");
                    sessionExpired("Error when contacting server. Please try again later.");
                }
            }
        }.execute();
    }



    public static Note getNote(String tag){
        return getInstance().notes.get(tag);
    }

    private static String getTitleFromNote(Note n){

        if(!n.getTitle().equals("")){
            return n.getTitle();
        }

        String content = n.getContent();
        int end = content.length()<100? content.length(): 100;
        for(int i=0; i<end; i++){
            if(content.charAt(i) == '\n'){
                end = i;
                break;
            }
        }
        return n.getContent().substring(0, end);
    }

    public static void decrementZ(String tag){
        for(Map.Entry<String, Note> cursor: getInstance().notes.entrySet()){
            if(cursor.getKey() != tag){
                cursor.getValue().decrementZ();
            }
        }
    }

    private static void switchToNote(Note n){
        n.moveToTop();
        decrementZ(n.getTag());
        Intent noteActivity = new Intent(getInstance().parent, NoteActivity.class);
        noteActivity.putExtra("Tag", n.getTag());
        noteActivity.putExtra("Content", n.getContent());
        getInstance().parent.startActivity(noteActivity);
    }

    private NoteManager(){}

    private static class SingletonHelper{
        private final static NoteManager instance = new NoteManager();
    }

    private static NoteManager getInstance(){ return SingletonHelper.instance; }

}
