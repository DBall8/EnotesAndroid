package edudcball.wpi.users.enotesandroid.data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import edudcball.wpi.users.enotesandroid.Callback;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.pageTasks.DeletePageTask;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.pageTasks.NewPageTask;
import edudcball.wpi.users.enotesandroid.data.classes.Note;
import edudcball.wpi.users.enotesandroid.data.classes.Page;
import edudcball.wpi.users.enotesandroid.data.classes.Sortable;
import edudcball.wpi.users.enotesandroid.exceptions.CustomException;
import edudcball.wpi.users.enotesandroid.observerPattern.IObserver;

public class PageManager {

    private SortedList<Page> pages;
    private int activePageIndex = 0;

    public PageManager(){
        pages = new SortedList<>();
        pages.setSortMode(SortedList.SortMode.INDEX);
    }

    // PRIVATE METHODS -----------------------------------------------------------------------------

    /**
     * Load all pages for the user at their current state
     * @param pageArray array of pages obtained from server
     * @return True if successful
     */
    public void load(JSONArray pageArray, JSONArray noteArray) throws CustomException{
        pages.clear();

        try{
            for(int i=0; i<pageArray.length(); i++){
                JSONObject pageJSON = pageArray.getJSONObject(i);
                Page page = new Page(pageJSON);
                pages.add(page);
            }

            Log.d("MYAPP", "PAGES LOADED");
        }
        catch(Exception e){
            Log.d("MYAPP", "Unable to form pages from response");
            Log.d("MYAPP", "LOAD PAGES FAILED: " + e.getMessage());
            throw new CustomException("Error when contacting server. Please try again later.", e.getMessage());
        }

        try{
            // Load each note in the http response
            for(int i=0; i<noteArray.length(); i++){
                JSONObject noteJSON = noteArray.getJSONObject(i);
                Note note = new Note(noteJSON);

                if (!pages.containsItemWithId(note.getPageID())){
                    Log.d("MYAPP", "Note with Page ID that does not exist");
                    throw new CustomException("Error when contacting server. Please try again later.", "Note with Page ID that does not exist");
                }

                // Place note into page's list
                pages.getItem(note.getPageID()).addNote(note);
            }
            Log.d("MYAPP", "NOTES LOADED");
        }
        catch(CustomException e){
            throw e;
        }
        catch(Exception e){
            Log.d("MYAPP", "Unable to form notes from response");
            Log.d("MYAPP", "LOAD NOTES FAILED: " + e.getMessage());
            throw new CustomException("Error when contacting server. Please try again later.", "Failed to convert note JSONs to notes");
        }

        Log.d("MYAPP", "Notes and pages successfully loaded");
        printPages();
    }

    // PUBLIC METHODS ------------------------------------------------------------------------------

    /**
     * Retrieves a page
     * @param pageId ID of the page to retrieve
     * @return  the Page object
     */
    public Page getPage(String pageId){
        return pages.getItem(pageId);
    }

    /**
     * Creates and adds a new page to the user's account
     * @param callback Code to run on completion
     * @return Callback returns the page if successful, or null if failed
     */
    public void createPage(final Callback<Page> callback){
        final Page newPage = new Page(pages.size());

        new NewPageTask(newPage, new Callback<String>() {
            @Override
            public void run(String param) {
                if(param == null){
                    Log.d("MYAPP", "Error when creating new Page.");
                    callback.run(null);
                }

                try{
                    JSONObject json = new JSONObject(param);
                    if(json.getBoolean("successful")){
                        addPage(newPage);
                        callback.run(newPage);
                    }
                    else{
                        callback.run(null);
                    }
                }
                catch(JSONException e){
                    e.printStackTrace();
                    callback.run(null);
                }
            }
        }).execute();
    }

    public void addPage(Page page){
        if (pages.containsItemWithId(page.getId())){
            Log.d("MYAPP", "Tried to add page with duplicate ID");
            return;
        }

        pages.add(page);
    }

    /**
     * Delete a page from the user's account
     * @param pageId ID of page to delete
     * @return true if successful
     */
    public void deletePage(final String pageId, final Callback<Boolean> callback){
        if(!pages.containsItemWithId(pageId)){
            Log.d("MYAPP", "Attempted to delete page that does not exist.");
            callback.run(false);
            return;
        }

        new DeletePageTask(pageId, new Callback<String>() {
            @Override
            public void run(String param) {
                if (param == null){
                    Log.d("MYAPP", "Delete note returned null");
                    callback.run(false);
                }

                try{
                    JSONObject json = new JSONObject(param);
                    if(json.getBoolean("successful")){
                        pages.remove(pageId);
                        callback.run(true);
                    }
                }
                catch(JSONException e){
                    e.printStackTrace();
                    callback.run(false);
                }
            }
        }).execute();
    }

    public void update(Callback<Boolean> callback){
        for(int i=0; i<pages.size(); i++){
            pages.getItem(i).update(callback);
        }
    }

    public void selectPage(int index){
        this.activePageIndex = index;
    }

    public void selectPage(String id){
        this.activePageIndex = pages.getItemIndex(id);
    }

    public void removePage(String id){
        pages.remove(id);
    }

    public Page getActivePage(){
        return pages.getItem(activePageIndex);
    }

    public List<String> getPageTitles(){ return pages.getTitleList(); }

    public Note findNote(String noteId){
        for (int i=0; i<pages.size(); i++){
            Note note = pages.getItem(i).getNote(noteId);
            if (note != null){
                return note;
            }
        }

        return null;
    }

    public Page findNoteOwner(String noteId){
        for (int i=0; i<pages.size(); i++){
            Page page = pages.getItem(i);
            if (page.getNote(noteId) != null){
                return page;
            }
        }

        return null;
    }

    // DEBUG ---------------------------------------------------------------------------------------
    public void printPages(){
        for (int i=0; i<pages.size(); i++){
            Page page = pages.getItem(i);
            Log.d("MYAPP", page.getId());
            page.printNotes();
        }
    }
}
