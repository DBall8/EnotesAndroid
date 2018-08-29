package edudcball.wpi.users.enotesandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
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
    private Context context;
    private ListView lv;

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
     */
    public static void init(final MainActivity parent, ListView lv){

        final NoteManager instance = getInstance();

        // save parent for screen switching
        instance.parent = parent;
        instance.context = parent.getApplicationContext();
        instance.lv = lv;

        // Load the noteAdapter used for displaying icons represeting notes that can be clicked on
        instance.buildNoteAdapter();

        // Set clicking on an item to open that note
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Note n = getNote(getInstance().noteTagLookup.get(i));
                getInstance().switchToNote(n);
            }
        });
    }

    public static void buildNoteAdapter(){
        final NoteManager instance = getInstance();
        instance.noteAdapter = new ArrayAdapter<String>(instance.context, android.R.layout.simple_list_item_1, instance.noteTitles){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(ContextCompat.getColor(instance.context, R.color.black));
                Drawable bg = ContextCompat.getDrawable(instance.context, R.drawable.note_icon);

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
                Note n = getNote(instance.noteTagLookup.get(position));
                // Set the background to match the note's color
                JSONObject colors = n.getColors();
                if(colors != null){
                    try {
                        bg.setColorFilter(Color.parseColor(colors.getString("body")), PorterDuff.Mode.MULTIPLY);
                        //tv.setBackgroundColor(Color.parseColor(colors.getString("body")));
                    }catch(Exception e){
                        tv.setBackgroundColor(instance.parent.getResources().getColor(R.color.defaultNote));
                    }
                }
                else{
                    tv.setBackgroundColor(instance.parent.getResources().getColor(R.color.defaultNote));
                }

                return tv;
            }
        };

        // Load the adapter for the listview
        instance.lv.setAdapter(instance.noteAdapter);
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
                sessionExpired(parent,"Session expired. Please log in again.");
                return;
            }

            // Clear everything, start from empty, except the notes list to preserve their z index
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
            sessionExpired(parent, "Error when contacting server. Please try again later.");
        }

    }

    /**
     * Redirects the user back to the login screen
     * @param message the message to display once back on the login screen
     */
    public static void sessionExpired(Activity activity, String message){
        Intent next = new Intent(activity, LoginActivity.class);
        next.putExtra("error", message);
        activity.startActivity(next);
    }


    /**
     * Builds the list of notes from the notes hashmap, adding them in order
     */
    private void createNoteList(){

        // Clear both lists being built
        noteTitles.clear();
        noteTagLookup.clear();

        // Rebuild lists depending on the current sorting settings
        switch(Settings.getSortBy()){
            case COLOR:
                getInstance().sortByColor();
                break;
            case RECENT:
            default:
                getInstance().sortByRecent();
        }

        // Notify the adatper that the list has been changed
        getInstance().noteAdapter.notifyDataSetChanged();
    }

    /**
     * Sorts the notes where the most recently viewed note is first
     */
    private void sortByRecent(){
        // Convert hashmap into a list
        ArrayList<String> workingList = new ArrayList<String>();
        for(Map.Entry<String, Note> cursor: notes.entrySet()){
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
                noteTitles.add(getTitleFromNote(note));
                noteTagLookup.add(maxTag);
            }

        }
    }

    /**
     * Sort notes by color (currently color order is set in stone)
     */
    private void sortByColor(){

        // The order of colors to add in
        String[] colorOrder = {"#ffe062", "#ffa63d", "#f97e7e", "#53ce57", "#7fc8f5", "#e083f7"};

        // Go through each color and add all notes of that color
        for(String color: colorOrder){
            // Go through hashmap and add all notes of the current color
            for(Map.Entry<String, Note> cursor: notes.entrySet()){
                try {
                    Note n = cursor.getValue();
                    String noteColor = n.getColors().getString("body");
                    if(color.equals(noteColor)){
                        noteTitles.add(getTitleFromNote(n));
                        noteTagLookup.add(cursor.getKey());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Adds a new note, locally and server side, and opens the note
     */
    public static void newNote(){

        // create and add new note
        final Note n = new Note();
        getInstance().notes.put(n.getTag(), n);

        // Send message to server about the new note
        new NewNoteTask(n){

            @Override
            protected void onPostExecute(String result) {

                // If connection lost, return to login
                if(result == null) {
                    sessionExpired(getInstance().parent, "Connection to server lost, please login again.");
                    return;
                }

                try{
                    // parse the server's response
                    JSONObject obj = new JSONObject(result);
                    // if session expired, return to login
                    if(obj.getBoolean("sessionExpired")){
                        Log.d("MYAPP", "Session expired");
                        sessionExpired(getInstance().parent, "Session expired. Please log in again.");
                        return;
                    }
                    else{
                        // Note addition succeeded! update the note's zindex and open the note
                        getInstance().topNoteZ++;
                        n.setZIndex(getInstance().topNoteZ);
                        getInstance().switchToNote(n);
                    }
                }
                catch(Exception e){
                    Log.d("MYAPP", "Unable to form response JSON for update notes");
                    sessionExpired(getInstance().parent,"Error when contacting server. Please try again later.");
                }
            }
        }.execute();
    }

    /**
     * Updates the server of the note's status
     * @param n the note to update the server about
     * @param callback the function handler to call when the response is received
     */
    public static void updateNote(Note n, final EventHandler<String> callback){
        // Call asynch update task
        new UpdateNoteTask(n){

            @Override
            protected void onPostExecute(String result) {
                // asynch task finished, call callback
                callback.handle(result);
            }
        }.execute();
    }

    /**
     * Tells the server to delete a note from the database
     * @param tag the tag of the note to delete
     * @param callback a funciton handler to call after the server responds
     */
    public static void deleteNote(final String tag, final EventHandler<String> callback){
        // Call a delete asynch task
        new DeleteTask(tag){
            @Override
            protected void onPostExecute(String result) {
                // task completed, call callback
                callback.handle(result);
            }
        }.execute();
    }

    /**
     * Removes a note from the local note list
     * @param tag the tag of the note to remove
     */
    /*
    public static void removeNote(String tag){
        getInstance().notes.remove(tag);
        for(int i=0; i< getInstance().noteTagLookup.size(); i++){
            if(getInstance().noteTagLookup.get(i).equals(tag)){
                getInstance().noteTagLookup.remove(i);
                getInstance().noteTitles.remove(i);
                getInstance().noteAdapter.notifyDataSetChanged();
                i--;
            }
        }
    }
    */

    /**
     * Retrieves a note from the hashmap
     * @param tag the tag of the note to retrieve
     * @return the note with the given tag
     */
    public static Note getNote(String tag){
        return getInstance().notes.get(tag);
    }

    /**
     * Creates a title to display on the note icon
     * @param n the note to create the title from
     * @return a title to display on the note icon
     */
    private String getTitleFromNote(Note n){

        // If the note has a title, use that
        if(!n.getTitle().equals("")){
            return n.getTitle();
        }

        // Otherwise, create a dummy title from the first 100 characters in its content's first line
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

    /**
     * Resets the zindex of every note to create the smallest z index range possible
     */
    private void restackNotes(){
        // noteTagLookup should already be sorted by zindex, so no need to resort.
        int len = noteTagLookup.size();
        for(int i=len-1; i>=0; i--){
            Note n = getNote(noteTagLookup.get(i));
            n.setZIndex(BOTTOMZ + i);
        }

        topNoteZ = BOTTOMZ + len;
    }

    /**
     * Starts a note activity
     * @param n the note to create the note activity for
     */
    private void switchToNote(Note n){

        // restack zindex if the zindex has gotten too large
        if(topNoteZ >= TOPZ){
            restackNotes();
        }

        // increment the top z and set the note to have that top z
        topNoteZ++;
        n.setZIndex(topNoteZ);

        // start the new note activity for the given note
        Intent noteActivity = new Intent(parent, NoteActivity.class);
        noteActivity.putExtra("Tag", n.getTag());
        parent.startActivity(noteActivity);
    }

    /**
     * Resorts the note list
     */
    public static void reSort(){
        getInstance().createNoteList();
    }

    // Cookie getter, setter, and resetter
    public static CookieStore getCookies(){ return getInstance().cookies.getCookieStore(); }
    public static void addCookies(String cookie){
        getInstance().cookies.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
    }
    public static void resetCookies(){ getInstance().cookies = new CookieManager(); }

    // singleton methods

    private NoteManager(){}

    private static class SingletonHelper{
        private final static NoteManager instance = new NoteManager();
    }

    private static NoteManager getInstance(){ return SingletonHelper.instance; }

}
