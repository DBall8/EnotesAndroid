package edudcball.wpi.users.enotesandroid.dataManagers;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.ArrayList;

import edudcball.wpi.users.enotesandroid.AsyncTasks.ConnectionTestTask;
import edudcball.wpi.users.enotesandroid.AsyncTasks.RetrieveNotesTask;
import edudcball.wpi.users.enotesandroid.EventHandler;
import edudcball.wpi.users.enotesandroid.activities.LoginActivity;
import edudcball.wpi.users.enotesandroid.objects.Note;
import edudcball.wpi.users.enotesandroid.objects.NotePage;

public class DataManager {

    protected Activity mainActivity;
    private String username = ""; // user's username
    private SocketConnection socket;
    private CookieManager cookies = new CookieManager(); // manages cookies

    private NoteManager noteManager = new NoteManager(this);
    private PageManager pageManager = new PageManager(this);

    public DataManager(){}


    /**
     * Retrieve all notes for the user from the server and then load them
     */
    public synchronized void retrieveData(final Activity a, final EventHandler<Void> callback) {
        new RetrieveNotesTask() {

            @Override
            protected void onPostExecute(String result) {
                if (result == null) {
                    Log.d("MYAPP", "Empty result");
                    sessionExpired(a, "Session expired. Please log in again.");
                    return;
                }
                mainActivity = a;
                if (load(a, result)) {
                    if (socket != null) socket.disconnect();
                    socket = new SocketConnection(noteManager.getNotes());
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
            pageManager.loadNotePages(activity, pageArr);

            // Load each note in the http response
            JSONArray noteArr = obj.getJSONArray("notes");
            noteManager.loadNotes(activity, noteArr);
            return true;
        }
        catch(Exception e){
            Log.d("MYAPP", "Unable to form response JSON for get notes");
            Log.d("MYAPP", "LOAD NOTES FAILED: " + e.getMessage());
            sessionExpired(activity, "Error when contacting server. Please try again later.");
            return false;
        }
    }

    /**
     * Redirects the user back to the login screen
     * @param message the message to display once back on the login screen
     */
    public void sessionExpired(Activity activity, String message){
        if(activity == null){
            activity = mainActivity;
        }
        Intent next = new Intent(activity, LoginActivity.class);
        next.putExtra("error", message);
        activity.startActivity(next);
    }

    void checkConnection(final Activity activity){
        new ConnectionTestTask(){
            @Override
            protected void onPostExecute(Boolean success) {
                if(!success){
                    sessionExpired(activity, "Session expired. Please log in again.");
                }
            }
        }.execute();
    }

    public String getUsername(){ return username; }
    public String getSocketID(){ return socket.getID(); }

    // Cookie getter, setter, and resetter
    public CookieStore getCookies(){ return cookies.getCookieStore(); }
    public void addCookies(String cookie){
        cookies.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
    }
    public void resetCookies(){ cookies = new CookieManager(); }

    // NOTE METHODS --------------------------------------------------------------------------------
    public void reSort(){ noteManager.reSort(); }
    public void newNote(final Activity activity){ noteManager.newNote(activity);}
    public void updateNote(final Activity activity, Note n, final EventHandler<String> callback){
        noteManager.updateNote(activity, n, callback);
    }
    public void deleteNote(final Activity activity, final String tag, final EventHandler<String> callback){
        noteManager.deleteNote(activity, tag, callback);
    }
    public Note getNote(String tag){ return noteManager.getNote(tag); }
    public Note getNote(int i){ return noteManager.getNote(i);}
    public void switchToNote(Activity a, int i){ noteManager.switchToNote(a, i); }
    public ArrayList<String> getNoteTitles(){ return noteManager.getNoteTitles(); }
    // ---------------------------------------------------------------------------------------------

    // PAGE METHODS --------------------------------------------------------------------------------
    public void newPage(Activity activity){ pageManager.newPage(activity);}
    public void updatePage(final Activity activity, NotePage page, final EventHandler<String> callback){
        pageManager.updatePage(activity, page, callback);
    }
    public void deletePage(final Activity activity, String pageID, final EventHandler<String> callback){
        pageManager.deletePage(activity, pageID, callback);
    }
    public NotePage getPage(String pageID){ return pageManager.getPage(pageID); }
    public NotePage switchToPage(Activity a, int i){ return  pageManager.switchToPage(a, i); }
    public ArrayList<String> getPageTitles(){ return pageManager.getPageTitles(); }
    public int getNumPages(){ return pageManager.getNumPages(); }
    public String getCurrentPageID(){ return pageManager.getCurrentPageID(); }
    synchronized void addPageToArray(NotePage page){ pageManager.addPageToArray(page);}
    synchronized void updatePageArray(NotePage page){ pageManager.updatePageArray(page);}
    synchronized void removePageFromArray(Activity activity,String pageID){ pageManager.removePageFromArray(activity, pageID);}
    // ---------------------------------------------------------------------------------------------
}
