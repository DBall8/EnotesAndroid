package edudcball.wpi.users.enotesandroid.dataManagers;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import edudcball.wpi.users.enotesandroid.AsyncTasks.pageTasks.DeletePageTask;
import edudcball.wpi.users.enotesandroid.AsyncTasks.pageTasks.NewPageTask;
import edudcball.wpi.users.enotesandroid.AsyncTasks.pageTasks.UpdatePageTask;
import edudcball.wpi.users.enotesandroid.EventHandler;
import edudcball.wpi.users.enotesandroid.activities.MainActivity;
import edudcball.wpi.users.enotesandroid.activities.NotePageActivity;
import edudcball.wpi.users.enotesandroid.objects.NotePage;

public class PageManager {

    private NotePage[] notePages = new NotePage[0];
    private ArrayList<String> pageTitles = new ArrayList<>();

    private String currentPageID = "";

    private DataManager dataManager;

    public PageManager(DataManager dataManager){
        this.dataManager = dataManager;
    }

    void loadNotePages(Activity activity, JSONArray arr){
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
                    notePages = reSortPages(activity, arr);
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
            dataManager.sessionExpired(activity, "Error when contacting server. Please try again later.");
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
            dataManager.sessionExpired(activity, "Error when contacting server. Please try again later.");
        }

        return notePages;
    }

    void newPage(final Activity activity){
        final NotePage page = new NotePage("");

        new NewPageTask(page){
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
                        addPageToArray(page);
                        switchToPage(activity, page, true);
                    }
                }
                catch(Exception e){
                    Log.d("MYAPP", "Unable to form response JSON for create page");
                    dataManager.sessionExpired(activity,"Error when contacting server. Please try again later.");
                }
            }
        }.execute();
    }

    synchronized void addPageToArray(NotePage page){
        NotePage[] newPages = new NotePage[notePages.length+1];
        for(int i=0; i<notePages.length; i++){
            newPages[i] = notePages[i];
        }
        newPages[newPages.length-1] = page;

        notePages = newPages;
        pageTitles.add(page.getName());
        MainActivity.notifyAdatperChanged();
    }

    void updatePage(final Activity activity, NotePage page, final EventHandler<String> callback){

        updatePageArray(page);
        MainActivity.notifyAdatperChanged();

        new UpdatePageTask(page){
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
                    if(callback != null) callback.handle(result);
                } catch (Exception e) {
                    Log.d("MYAPP", "Unable to form response JSON for update pages");
                    dataManager.sessionExpired(activity, "Error when contacting server. Please try again later.");
                }
            }
        }.execute();
    }

    synchronized void updatePageArray(NotePage page){
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

            pageTitles.clear();
            for(NotePage p: notePages){
                pageTitles.add(p.getName());
            }
            MainActivity.notifyAdatperChanged();
        }
    }

    void deletePage(final Activity activity, String pageID, final EventHandler<String> callback){

        removePageFromArray(activity, pageID);

        // Call a delete asynch task
        new DeletePageTask(pageID){
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
                    Log.d("MYAPP", "Unable to form response JSON for delete page");
                    dataManager.sessionExpired(activity, "Error when contacting server. Please try again later.");
                }

            }
        }.execute();
    }

    synchronized void removePageFromArray(Activity activity,String pageID){

        NotePage[] newPages = new NotePage[notePages.length-1];
        int index = 0;
        for(NotePage p: notePages){
            if(p != null && !p.getPageID().equals(pageID) && index < newPages.length){
                newPages[index] = p;
                index++;
            }
        }

        notePages = newPages;
        if(activity != null){
            reIndexPages(activity);
        }
        else{
            reIndexPages(dataManager.mainActivity);
        }

        pageTitles.clear();
        for(NotePage p: newPages){
            pageTitles.add(p.getName());
        }
        MainActivity.notifyAdatperChanged();
    }

    NotePage getPage(String pageID){
        for(NotePage p: notePages){
            if(p != null && p.getPageID().equals(pageID)){
                return p;
            }
        }

        return null;
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

    private void switchToPage(final Activity a, final NotePage page, final boolean isNew){

        dataManager.checkConnection(a);

        currentPageID = page.getPageID();
        dataManager.reSort();
        Intent intent = new Intent(a, NotePageActivity.class);
        intent.putExtra("pageID", page.getPageID());
        intent.putExtra("new", isNew);
        a.startActivity(intent);
    }

    NotePage switchToPage(Activity a, int i){
        if(i >= notePages.length) return null;

        NotePage page = notePages[i];

        switchToPage(a, page, false);

        return page;
    }

    ArrayList<String> getPageTitles(){ return pageTitles; }
    NotePage[] getCurrentNotePages(){ return notePages; }
    int getNumPages(){ return notePages.length; }

    String getCurrentPageID(){ return currentPageID; }
}
