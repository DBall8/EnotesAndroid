package edudcball.wpi.users.enotesandroid;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
import java.net.CookieStore;
import java.net.HttpCookie;
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

    private final static int TOPZ = 9999;
    private final static int BOTTOMZ = 100;

    private MainActivity parent;

    private HashMap<String, Note> notes = new HashMap<>(); // Map of notes by their tag
    private ArrayList<String> noteTagLookup = new ArrayList<>(); // Maps note tags to their index in the noteAdapter
    private ArrayList<String> noteTitles = new ArrayList<>(); // The list of note titles to display in the list view

    private ArrayAdapter<String> noteAdapter; // The array adapter for displaying notes in the list view
    private String username = ""; // user's username

    private CookieManager cookies = new CookieManager(); // manages cookies

    private int topNoteZ = BOTTOMZ;

    /**
     * Initializes the NoteManager and attaches it to a list view for displaying notes
     * @param parent the calling activity
     * @param lv the list view for displaying notes
     * @param context the context (was a while ago)
     */
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
                Drawable bg = ContextCompat.getDrawable(context, R.drawable.note_icon);
                tv.setBackground(bg);

                // Get the note by its position in the list
                Note n = getNote(getInstance().noteTagLookup.get(position));
                // Set the background to match the note's color
                JSONObject colors = n.getColors();
                if(colors != null){
                    try {
                        bg.setColorFilter(Color.parseColor(colors.getString("body")), PorterDuff.Mode.MULTIPLY);
                        //tv.setBackgroundColor(Color.parseColor(colors.getString("body")));
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
                getInstance().switchToNote(n);
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
                if(result == null) return;

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
            notes.clear();

            // store the user's username
            username = obj.getString("username");

            // Load each note in the http response
            JSONArray arr = obj.getJSONArray("notes");
            for(int i=0; i<arr.length(); i++){
                JSONObject noteJSON = arr.getJSONObject(i);
                Note n = new Note(noteJSON);
                notes.put(n.getTag(), n);
                if(n.getZ() > topNoteZ){
                    topNoteZ = n.getZ();
                }
            }
            createNoteList();
            Log.d("MYAPP", "NOTES LOADED");
        }
        catch(Exception e){
            Log.d("MYAPP", "Unable to form response JSON for get notes");
            Log.d("MYAPP", "LOAD NOTES FAILED: " + e.getMessage());
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

    public static void newNote(){

        final Note n = new Note();
        getInstance().notes.put(n.getTag(), n);

        new NewNoteTask(n){

            @Override
            protected void onPostExecute(String result) {

                if(result == null) return;

                try{

                    JSONObject obj = new JSONObject(result);
                    if(obj.getBoolean("sessionExpired")){
                        Log.d("MYAPP", "Session expired");
                        sessionExpired("Session expired. Please log in again.");
                        return;
                    }
                    else{
                        getInstance().topNoteZ++;
                        n.setZIndex(getInstance().topNoteZ);
                        getInstance().switchToNote(n);
                    }
                }
                catch(Exception e){
                    Log.d("MYAPP", "Unable to form response JSON for update notes");
                    sessionExpired("Error when contacting server. Please try again later.");
                }
            }
        }.execute();
    }

    public static void updateNote(String tag){
        Note n = getNote(tag);
        new UpdateNoteTask(n){

            @Override
            protected void onPostExecute(String result) {

                if(result == null) return;

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

                if(result == null) return;

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

    private String getTitleFromNote(Note n){

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

    private void restackNotes(){
        // noteTagLookup should already be sorted by zindex, so no need to resort.
        int len = noteTagLookup.size();
        for(int i=len-1; i>=0; i--){
            Note n = getNote(noteTagLookup.get(i));
            n.setZIndex(BOTTOMZ + i);
        }

        topNoteZ = BOTTOMZ + len;
    }

    private void switchToNote(Note n){

        if(topNoteZ >= TOPZ){
            restackNotes();
        }

        topNoteZ++;
        n.setZIndex(topNoteZ);

        Intent noteActivity = new Intent(parent, NoteActivity.class);
        noteActivity.putExtra("Tag", n.getTag());
        parent.startActivity(noteActivity);
    }

    public static CookieStore getCookies(){ return getInstance().cookies.getCookieStore(); }
    public static void addCookies(String cookie){
        getInstance().cookies.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
    }
    public static void resetCookies(){ getInstance().cookies = new CookieManager(); }

    private NoteManager(){}

    private static class SingletonHelper{
        private final static NoteManager instance = new NoteManager();
    }

    private static NoteManager getInstance(){ return SingletonHelper.instance; }

}
