package edudcball.wpi.users.enotesandroid.dataManagers;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edudcball.wpi.users.enotesandroid.AsyncTasks.ConnectionTestTask;
import edudcball.wpi.users.enotesandroid.AsyncTasks.noteTasks.DeleteTask;
import edudcball.wpi.users.enotesandroid.AsyncTasks.noteTasks.NewNoteTask;
import edudcball.wpi.users.enotesandroid.AsyncTasks.RetrieveNotesTask;
import edudcball.wpi.users.enotesandroid.AsyncTasks.noteTasks.UpdateNoteTask;
import edudcball.wpi.users.enotesandroid.AsyncTasks.pageTasks.DeletePageTask;
import edudcball.wpi.users.enotesandroid.AsyncTasks.pageTasks.NewPageTask;
import edudcball.wpi.users.enotesandroid.AsyncTasks.pageTasks.UpdatePageTask;
import edudcball.wpi.users.enotesandroid.EventHandler;
import edudcball.wpi.users.enotesandroid.activities.LoginActivity;
import edudcball.wpi.users.enotesandroid.activities.MainActivity;
import edudcball.wpi.users.enotesandroid.activities.NoteActivity;
import edudcball.wpi.users.enotesandroid.activities.NotePageActivity;
import edudcball.wpi.users.enotesandroid.objects.Note;
import edudcball.wpi.users.enotesandroid.objects.NotePage;

/**
 * Created by Owner on 1/5/2018.
 */

public class NoteManager {

    private final static int TOPZ = 9999;
    private final static int BOTTOMZ = 100;

    private HashMap<String, Note> notes = new HashMap<>(); // Map of notes by their tag
    private ArrayList<String> noteTagLookup = new ArrayList<>(); // Maps note tags to their index in the noteAdapter
    private ArrayList<String> noteTitles = new ArrayList<>(); // The list of note titles to display in the list view

    private int topNoteZ = BOTTOMZ;

    private DataManager dataManager;

    public NoteManager(DataManager dataManager){
        this.dataManager = dataManager;
    }

    /**
     * Load all notes for the user onto the screen
     * @param arr a json array of note json objects
     */
    void loadNotes(Activity activity, JSONArray arr){

        try{
            // Clear everything, start from empty, except the notes list to preserve their z index
            notes.clear();

            // Load each note in the http response
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
            Log.d("MYAPP", "Unable to form notes from response");
            Log.d("MYAPP", "LOAD NOTES FAILED: " + e.getMessage());
            dataManager.sessionExpired(activity, "Error when contacting server. Please try again later.");
        }

    }

    /**
     * Builds the list of notes from the notes hashmap, adding them in order
     */
    private void createNoteList(){

        // Clear both lists being built
        noteTitles.clear();
        noteTagLookup.clear();

        // Rebuild lists depending on the current sorting settings
        switch(MainActivity.getSettings().getSortBy()){
            case COLOR:
                sortByColor();
                break;
            case RECENT:
            default:
                sortByRecent();
                break;
            case ALPHA:
                sortByAlpha();
        }

        // Notify the adapter that the list has been changed
        NotePageActivity.updateNoteAdapter();
    }

    /**
     * Sorts the notes where the most recently viewed note is first
     */
    private void sortByRecent(){
        // Convert hashmap into a list
        ArrayList<String> workingList = new ArrayList<String>();
        for(Map.Entry<String, Note> cursor: notes.entrySet()){
            if(cursor.getValue().getPageID().equals(dataManager.getCurrentPageID())) {
                workingList.add(cursor.getKey());
            }
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
                noteTitles.add(note.getTitleForDisplay());
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
                if(!cursor.getValue().getPageID().equals(dataManager.getCurrentPageID())) continue;
                try {
                    Note n = cursor.getValue();
                    String noteColor = n.getColors().getString("body");
                    if(color.equals(noteColor)){
                        noteTitles.add(n.getTitleForDisplay());
                        noteTagLookup.add(cursor.getKey());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sortByAlpha(){
        // Convert hashmap into a list
        ArrayList<String> workingList = new ArrayList<String>();
        for(Map.Entry<String, Note> cursor: notes.entrySet()){
            if(cursor.getValue().getPageID().equals(dataManager.getCurrentPageID())) {
                workingList.add(cursor.getKey());
            }
        }

        int len = workingList.size();

        String firstTitle;
        int titleAlpha;
        int a;
        // Loop through and add the note with the highest z index until all notes are added
        for(int i=0; i<len; i++){

            String firstTag = workingList.get(0);
            firstTitle = getNote(firstTag).getTitleForDisplay();
            titleAlpha = Character.toLowerCase(firstTitle.charAt(0));
            for(int j=1; j<workingList.size(); j++){
                String title = getNote(workingList.get(j)).getTitleForDisplay();
                a = Character.toLowerCase(title.charAt(0));
                if(a < titleAlpha){
                    firstTag = workingList.get(j);
                    firstTitle = title;
                    titleAlpha = a;
                }
            }
            // Add the largest zindex note to the titles list and remove it from the workingList
            if(firstTag != null){
                workingList.remove(firstTag);
                noteTitles.add(firstTitle);
                noteTagLookup.add(firstTag);
            }

        }
    }

    /**
     * Adds a new note, locally and server side, and opens the note
     */
    void newNote(final Activity activity){

        // create and add new note
        final Note n = new Note();
        notes.put(n.getTag(), n);
        reSort();

        // Send message to server about the new note
        new NewNoteTask(n){

            @Override
            protected void onPostExecute(String result) {

                // If connection lost, return to login
                if(result == null) {
                    dataManager.sessionExpired(activity, "Connection to server lost, please login again.");
                    return;
                }

                try{
                    // parse the server's response
                    JSONObject obj = new JSONObject(result);
                    // if session expired, return to login
                    if(obj.getBoolean("sessionExpired")){
                        Log.d("MYAPP", "Session expired");
                        dataManager.sessionExpired(activity, "Session expired. Please log in again.");
                        return;
                    }
                    else{
                        // Note addition succeeded! update the note's zindex and open the note
                        topNoteZ++;
                        n.setZIndex(topNoteZ);
                        switchToNote(activity, n);
                    }
                }
                catch(Exception e){
                    Log.d("MYAPP", "Unable to form response JSON for update notes");
                    dataManager.sessionExpired(activity,"Error when contacting server. Please try again later.");
                }
            }
        }.execute();
    }

    /**
     * Updates the server of the note's status
     * @param n the note to update the server about
     * @param callback the function handler to call when the response is received
     */
    void updateNote(final Activity activity, Note n, final EventHandler<String> callback){

        reSort();

        // Call asynch update task
        new UpdateNoteTask(n){

            @Override
            protected void onPostExecute(String result) {
                if(result == null){
                    dataManager.sessionExpired(activity, "Connection to server lost, please login again.");
                    return;
                }

                try {
                    JSONObject obj = new JSONObject(result);
                    if (obj.getBoolean("sessionExpired")) {
                        Log.d("MYAPP", "Session expired");
                        dataManager.sessionExpired(activity,"Session expired. Please log in again.");
                        return;
                    }
                    // asynch task finished, call callback
                    if(callback != null) callback.handle(result);
                } catch (Exception e) {
                    Log.d("MYAPP", "Unable to form response JSON for update notes");
                    dataManager.sessionExpired(activity, "Error when contacting server. Please try again later.");
                }

            }
        }.execute();
    }

    /**
     * Tells the server to delete a note from the database
     * @param tag the tag of the note to delete
     * @param callback a funciton handler to call after the server responds
     */
    void deleteNote(final Activity activity, final String tag, final EventHandler<String> callback){

        if(notes.containsKey(tag)){
            notes.remove(tag);
            reSort();
        }

        // Call a delete asynch task
        new DeleteTask(tag){
            @Override
            protected void onPostExecute(String result) {
                if(result == null){
                    dataManager.sessionExpired(activity, "Connection to server lost, please login again.");
                    return;
                }

                try{
                    JSONObject obj = new JSONObject(result);
                    if(obj.getBoolean("sessionExpired")){
                        Log.d("MYAPP", "Session expired");
                        dataManager.sessionExpired(activity, "Session expired. Please log in again.");
                        return;
                    }
                    // task completed, call callback
                    if(callback != null) callback.handle(result);
                }
                catch(Exception e){
                    Log.d("MYAPP", "Unable to form response JSON for delete note");
                    dataManager.sessionExpired(activity, "Error when contacting server. Please try again later.");
                }

            }
        }.execute();
    }

    /**
     * Retrieves a note from the hashmap
     * @param tag the tag of the note to retrieve
     * @return the note with the given tag
     */
    Note getNote(String tag){
        return notes.get(tag);
    }

    Note getNote(int i){
        return notes.get(noteTagLookup.get(i));
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
    private void switchToNote(Activity a, Note n){

        dataManager.checkConnection(a);

        // restack zindex if the zindex has gotten too large
        if(topNoteZ >= TOPZ){
            restackNotes();
        }

        // increment the top z and set the note to have that top z
        topNoteZ++;
        n.setZIndex(topNoteZ);

        // start the new note activity for the given note
        Intent noteActivity = new Intent(a, NoteActivity.class);
        noteActivity.putExtra("Tag", n.getTag());
        a.startActivity(noteActivity);
    }

    void switchToNote(Activity a, int i){
        switchToNote(a, getNote(i));
    }

    ArrayList<String> getNoteTitles(){ return noteTitles; }

    /**
     * Resorts the note list
     */
    public void reSort(){
        createNoteList();
    }

    HashMap<String, Note> getNotes(){ return this.notes; }

}
