package edudcball.wpi.users.enotesandroid.NoteManager;

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
import edudcball.wpi.users.enotesandroid.Settings;
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

    private Activity mainActivity;

    private HashMap<String, Note> notes = new HashMap<>(); // Map of notes by their tag
    private ArrayList<String> noteTagLookup = new ArrayList<>(); // Maps note tags to their index in the noteAdapter
    private ArrayList<String> noteTitles = new ArrayList<>(); // The list of note titles to display in the list view

    private NotePage[] notePages;
    private ArrayList<String> pageTitles = new ArrayList<>();

    private SocketConnection socket;

    private String username = ""; // user's username
    private String currentPageID = "";

    private CookieManager cookies = new CookieManager(); // manages cookies

    private int topNoteZ = BOTTOMZ;


    /**
     * Retrieve all notes for the user from the server and then load them
     */
    public synchronized static void retrieveNotes(final Activity a, final EventHandler<Void> callback){
        new RetrieveNotesTask(){

            @Override
            protected void onPostExecute(String result) {
                if(result == null){
                    Log.d("MYAPP", "Empty result");
                    sessionExpired(a,"Session expired. Please log in again.");
                    return;
                }
                getInstance().mainActivity = a;
                if(getInstance().load(a, result)) {
                    if(getInstance().socket != null) getInstance().socket.disconnect();
                    getInstance().socket = new SocketConnection(getInstance().notes);
                    if (callback != null) callback.handle(null);
                }
            }
        }.execute();
    }

    private boolean load(Activity activity, String res){
        try{
            JSONObject obj = new JSONObject(res);
            if(obj.getBoolean("sessionExpired")){
                Log.d("MYAPP", "Session expired");
                sessionExpired(activity,"Session expired. Please log in again.");
                return false;
            }

            // store the user's username
            username = obj.getString("username");

            JSONArray pageArr = obj.getJSONArray("notePages");
            loadNotePages(activity, pageArr);

            // Load each note in the http response
            JSONArray noteArr = obj.getJSONArray("notes");
            loadNotes(activity, noteArr);
            return true;
        }
        catch(Exception e){
            Log.d("MYAPP", "Unable to form response JSON for get notes");
            Log.d("MYAPP", "LOAD NOTES FAILED: " + e.getMessage());
            sessionExpired(activity, "Error when contacting server. Please try again later.");
            return false;
        }
    }

    private void loadNotePages(Activity activity, JSONArray arr){
        try{
            // Clear everything, start from empty, except the notes list to preserve their z index
            notePages = new NotePage[arr.length()];
            pageTitles.clear();
            // Load each note in the http response
            for(int i=0; i<notePages.length; i++){
                JSONObject pageJSON = arr.getJSONObject(i);
                NotePage p = new NotePage(pageJSON);
                if(p.getIndex() < notePages.length){
                    notePages[p.getIndex()] = p;
                }
            }

            for(NotePage p: notePages){
                if(p == null){
                    // Page indices got messed up, reset order
                    Log.d("MYAPP", "RE_INDEXING PAGES");
                    getInstance().notePages = getInstance().reSortPages(activity, arr);
                    break;
                }
                pageTitles.add(p.getName());
            }

            MainActivity.notifyAdatperChanged();

            Log.d("MYAPP", "PAGES LOADED");
        }
        catch(Exception e){
            Log.d("MYAPP", "Unable to form notes from response");
            Log.d("MYAPP", "LOAD PAGES FAILED: " + e.getMessage());
            sessionExpired(activity, "Error when contacting server. Please try again later.");
        }
    }

    /**
     * Re-orders pages according to their index and sets their index equal to the new order
     */
    private NotePage[] reSortPages(Activity activity, JSONArray arr){
        int len = arr.length();
        notePages = new NotePage[len];
        try {
            // Create a list of each note page object
            for (int i = 0; i < len; i++) {
                JSONObject pageJSON = arr.getJSONObject(i);
                NotePage p = new NotePage(pageJSON);
                notePages[i] = p;
            }

            // Sort note pages by index
            for (int i = 0; i < len-1; i++) {
                for (int j = 0; j < len - i - 1; j++) {
                    if (notePages[j].getIndex() > notePages[j + 1].getIndex()) {
                        // swap temp and arr[i]
                        NotePage temp = notePages[j];
                        notePages[j] = notePages[j + 1];
                        notePages[j + 1] = temp;
                    }
                }
            }

            reIndexPages(activity);
        }
        catch(Exception e){
            Log.d("MYAPP", "Unable to form notes from response");
            Log.d("MYAPP", "LOAD PAGES FAILED: " + e.getMessage());
            sessionExpired(activity, "Error when contacting server. Please try again later.");
        }

        return notePages;
    }

    /**
     * Load all notes for the user onto the screen
     * @param arr a json array of note json objects
     */
    private void loadNotes(Activity activity, JSONArray arr){

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
            sessionExpired(activity, "Error when contacting server. Please try again later.");
        }

    }

    /**
     * Redirects the user back to the login screen
     * @param message the message to display once back on the login screen
     */
    public static void sessionExpired(Activity activity, String message){
        if(activity == null){
            activity = getInstance().mainActivity;
        }
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
                break;
            case ALPHA:
                getInstance().sortByAlpha();
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
            if(cursor.getValue().getPageID().equals(currentPageID)) {
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
                if(!cursor.getValue().getPageID().equals(currentPageID)) continue;
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
            if(cursor.getValue().getPageID().equals(currentPageID)) {
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
    public static void newNote(final Activity activity){

        // create and add new note
        final Note n = new Note();
        getInstance().notes.put(n.getTag(), n);
        reSort();

        // Send message to server about the new note
        new NewNoteTask(n){

            @Override
            protected void onPostExecute(String result) {

                // If connection lost, return to login
                if(result == null) {
                    sessionExpired(activity, "Connection to server lost, please login again.");
                    return;
                }

                try{
                    // parse the server's response
                    JSONObject obj = new JSONObject(result);
                    // if session expired, return to login
                    if(obj.getBoolean("sessionExpired")){
                        Log.d("MYAPP", "Session expired");
                        sessionExpired(activity, "Session expired. Please log in again.");
                        return;
                    }
                    else{
                        // Note addition succeeded! update the note's zindex and open the note
                        getInstance().topNoteZ++;
                        n.setZIndex(getInstance().topNoteZ);
                        getInstance().switchToNote(activity, n);
                    }
                }
                catch(Exception e){
                    Log.d("MYAPP", "Unable to form response JSON for update notes");
                    sessionExpired(activity,"Error when contacting server. Please try again later.");
                }
            }
        }.execute();
    }

    /**
     * Updates the server of the note's status
     * @param n the note to update the server about
     * @param callback the function handler to call when the response is received
     */
    public static void updateNote(final Activity activity, Note n, final EventHandler<String> callback){

        reSort();

        // Call asynch update task
        new UpdateNoteTask(n){

            @Override
            protected void onPostExecute(String result) {
                if(result == null){
                    NoteManager.sessionExpired(activity, "Connection to server lost, please login again.");
                    return;
                }

                try {
                    JSONObject obj = new JSONObject(result);
                    if (obj.getBoolean("sessionExpired")) {
                        Log.d("MYAPP", "Session expired");
                        NoteManager.sessionExpired(activity,"Session expired. Please log in again.");
                        return;
                    }
                    // asynch task finished, call callback
                    if(callback != null) callback.handle(result);
                } catch (Exception e) {
                    Log.d("MYAPP", "Unable to form response JSON for update notes");
                    NoteManager.sessionExpired(activity, "Error when contacting server. Please try again later.");
                }

            }
        }.execute();
    }

    /**
     * Tells the server to delete a note from the database
     * @param tag the tag of the note to delete
     * @param callback a funciton handler to call after the server responds
     */
    public static void deleteNote(final Activity activity, final String tag, final EventHandler<String> callback){

        if(getInstance().notes.containsKey(tag)){
            getInstance().notes.remove(tag);
            reSort();
        }

        // Call a delete asynch task
        new DeleteTask(tag){
            @Override
            protected void onPostExecute(String result) {
                if(result == null){
                    NoteManager.sessionExpired(activity, "Connection to server lost, please login again.");
                    return;
                }

                try{
                    JSONObject obj = new JSONObject(result);
                    if(obj.getBoolean("sessionExpired")){
                        Log.d("MYAPP", "Session expired");
                        NoteManager.sessionExpired(activity, "Session expired. Please log in again.");
                        return;
                    }
                    // task completed, call callback
                    if(callback != null) callback.handle(result);
                }
                catch(Exception e){
                    Log.d("MYAPP", "Unable to form response JSON for delete note");
                    NoteManager.sessionExpired(activity, "Error when contacting server. Please try again later.");
                }

            }
        }.execute();
    }

    /**
     * Retrieves a note from the hashmap
     * @param tag the tag of the note to retrieve
     * @return the note with the given tag
     */
    public static Note getNote(String tag){
        return getInstance().notes.get(tag);
    }

    public static Note getNote(int i){
        return getInstance().notes.get(getInstance().noteTagLookup.get(i));
    }

    public static void newPage(final Activity activity){
        final NotePage page = new NotePage("");

        new NewPageTask(page){
            @Override
            protected void onPostExecute(String result) {
                // If connection lost, return to login
                if(result == null) {
                    sessionExpired(activity, "Connection to server lost, please login again.");
                    return;
                }

                try{
                    // parse the server's response
                    JSONObject obj = new JSONObject(result);
                    // if session expired, return to login
                    if(obj.getBoolean("sessionExpired")){
                        Log.d("MYAPP", "Session expired");
                        sessionExpired(activity, "Session expired. Please log in again.");
                        return;
                    }
                    else{
                        // Note addition succeeded! update the note's zindex and open the note
                        addPageToArray(page);
                        getInstance().switchToPage(activity, page, true);
                    }
                }
                catch(Exception e){
                    Log.d("MYAPP", "Unable to form response JSON for create page");
                    sessionExpired(activity,"Error when contacting server. Please try again later.");
                }
            }
        }.execute();
    }

    synchronized static void addPageToArray(NotePage page){
        NotePage[] newPages = new NotePage[getInstance().notePages.length+1];
        for(int i=0; i<getInstance().notePages.length; i++){
            newPages[i] = getInstance().notePages[i];
        }
        newPages[newPages.length-1] = page;

        getInstance().notePages = newPages;
        getInstance().pageTitles.add(page.getName());
    }

    public static void updatePage(final Activity activity, NotePage page, final EventHandler<String> callback){

        updatePageArray(page);
        MainActivity.notifyAdatperChanged();

        new UpdatePageTask(page){
            @Override
            protected void onPostExecute(String result) {
                if(result == null){
                    NoteManager.sessionExpired(activity, "Connection to server lost, please login again.");
                    return;
                }

                try {
                    JSONObject obj = new JSONObject(result);
                    if (obj.getBoolean("sessionExpired")) {
                        Log.d("MYAPP", "Session expired");
                        NoteManager.sessionExpired(activity,"Session expired. Please log in again.");
                        return;
                    }
                    if(callback != null) callback.handle(result);
                } catch (Exception e) {
                    Log.d("MYAPP", "Unable to form response JSON for update pages");
                    NoteManager.sessionExpired(activity, "Error when contacting server. Please try again later.");
                }
            }
        }.execute();
    }

    static synchronized void updatePageArray(NotePage page){
        Log.d("MYAPP", "START UPDATE");
        NotePage[] notePages = getInstance().notePages;
        NotePage oldPage = null;
        // Does the page at this page's index match the page?
        if(page.getIndex() < notePages.length && notePages[page.getIndex()].getPageID().equals(page.getPageID())){
            oldPage = notePages[page.getIndex()];
        }
        // The page wasnt at the same index, search the list instead
        else{
            for(int i=0; i< notePages.length; i++){
                if(notePages[i].getPageID().equals(page.getPageID())){
                    oldPage = notePages[i];
                    break;
                }
            }
        }
        // If the index of the page to be updated was found, update that page.
        if(oldPage != null){
            oldPage.setIndex(page.getIndex());
            oldPage.setName(page.getName());

            getInstance().pageTitles.clear();
            for(NotePage p: notePages){
                getInstance().pageTitles.add(p.getName());
            }
            MainActivity.notifyAdatperChanged();
        }
        Log.d("MYAPP", "END UPDATE");
    }

    public static void deletePage(final Activity activity, String pageID, final EventHandler<String> callback){

        removePageFromArray(activity, pageID);

        // Call a delete asynch task
        new DeletePageTask(pageID){
            @Override
            protected void onPostExecute(String result) {
                if(result == null){
                    NoteManager.sessionExpired(activity, "Connection to server lost, please login again.");
                    return;
                }

                try{
                    JSONObject obj = new JSONObject(result);
                    if(obj.getBoolean("sessionExpired")){
                        Log.d("MYAPP", "Session expired");
                        NoteManager.sessionExpired(activity, "Session expired. Please log in again.");
                        return;
                    }
                    // task completed, call callback
                    if(callback != null) callback.handle(result);
                }
                catch(Exception e){
                    Log.d("MYAPP", "Unable to form response JSON for delete page");
                    NoteManager.sessionExpired(activity, "Error when contacting server. Please try again later.");
                }

            }
        }.execute();
    }

    synchronized static void removePageFromArray(Activity activity,String pageID){

        NotePage[] newPages = new NotePage[getInstance().notePages.length-1];
        int index = 0;
        for(NotePage p: getInstance().notePages){
            if(p != null && !p.getPageID().equals(pageID) && index <= newPages.length){
                newPages[index] = p;
                index++;
            }
        }

        getInstance().notePages = newPages;
        if(activity != null){
            getInstance().reIndexPages(activity);
        }
        else{
            getInstance().reIndexPages(getInstance().mainActivity);
        }

        getInstance().pageTitles.clear();
        for(NotePage p: newPages){
            getInstance().pageTitles.add(p.getName());
        }
        MainActivity.notifyAdatperChanged();
    }

    public static NotePage getPage(String pageID){
        for(NotePage p: getInstance().notePages){
            if(p != null && p.getPageID().equals(pageID)){
                return p;
            }
        }

        return null;
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


    private void reIndexPages(Activity activity){
        // update all pages to have their index match their array position
        for(int i=0; i<notePages.length; i++){
            if(notePages[i].getIndex() != i){
                notePages[i].setIndex(i);
                updatePage(activity, notePages[i], null);
            }
        }
    }


    /**
     * Starts a note activity
     * @param n the note to create the note activity for
     */
    private void switchToNote(Activity a, Note n){

        checkConnection(a);

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

    public static void switchToNote(Activity a, int i){
        getInstance().switchToNote(a, getNote(i));
    }


    private void switchToPage(final Activity a, final NotePage page, final boolean isNew){

        checkConnection(a);

        currentPageID = page.getPageID();
        reSort();
        Intent intent = new Intent(a, NotePageActivity.class);
        intent.putExtra("pageID", page.getPageID());
        intent.putExtra("new", isNew);
        a.startActivity(intent);
    }

    public static NotePage switchToPage(Activity a, int i){
        if(i >= getInstance().notePages.length) return null;

        NotePage page = getInstance().notePages[i];

        getInstance().switchToPage(a, page, false);

        return page;
    }

    private void checkConnection(final Activity activity){
        new ConnectionTestTask(){
            @Override
            protected void onPostExecute(Boolean success) {
                if(!success){
                    sessionExpired(activity, "Session expired. Please log in again.");
                }
            }
        }.execute();
    }


    public static ArrayList<String> getNoteTitles(){ return getInstance().noteTitles; }
    public static ArrayList<String> getPageTitles(){ return getInstance().pageTitles; }
    static NotePage[] getCurrentNotePages(){ return getInstance().notePages; }
    public static int getNumPages(){ return getInstance().notePages.length; }
    public static String getUsername(){ return getInstance().username; }
    public static String getSocketID(){ return getInstance().socket.getID(); }
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

    public static String getCurrentPageID(){ return getInstance().currentPageID; }

    // singleton methods

    private NoteManager(){}

    private static class SingletonHelper{
        private final static NoteManager instance = new NoteManager();
    }

    private static NoteManager getInstance(){ return SingletonHelper.instance; }

}
