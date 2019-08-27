package edudcball.wpi.users.enotesandroid.data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import edudcball.wpi.users.enotesandroid.data.classes.Note;
import edudcball.wpi.users.enotesandroid.data.classes.Page;
import edudcball.wpi.users.enotesandroid.exceptions.CustomException;

public class PageManager {

    private HashMap<String, Page> pages;
    private String activePageId = "";

    public PageManager(JSONArray pageArray, JSONArray noteArray) throws CustomException{
        load(pageArray, noteArray);
    }

    // PRIVATE METHODS -----------------------------------------------------------------------------

    /**
     * Load all pages for the user at their current state
     * @param pageArray array of pages obtained from server
     * @return True if successful
     */
    private void load(JSONArray pageArray, JSONArray noteArray) throws CustomException{
        pages = new HashMap<>();

        try{
            for(int i=0; i<pageArray.length(); i++){
                JSONObject pageJSON = pageArray.getJSONObject(i);
                Page page = new Page(pageJSON);
                pages.put(page.getPageID(), page);
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

                if (!pages.containsKey(note.getPageID())){
                    Log.d("MYAPP", "Note with Page ID that does not exist");
                    throw new CustomException("Error when contacting server. Please try again later.", "Note with Page ID that does not exist");
                }

                // Place note into page's list
                pages.get(note.getPageID()).loadNote(note);
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
        return null;
    }

    /**
     * Adds a new page to the user's account
     * @param newPage page to add
     * @return true if successful
     */
    public boolean addPage(Page newPage){
        return true;
    }

    /**
     * Delete a page from the user's account
     * @param pageId ID of page to delete
     * @return true if successful
     */
    public boolean deletePage(String pageId){
        return true;
    }

    public void selectPage(String pageId){
        this.activePageId = pageId;
    }

    public Page getActivePage(){
        return pages.get(activePageId);
    }

    // DEBUG ---------------------------------------------------------------------------------------
    public void printPages(){
        for (Map.Entry<String, Page> entry: pages.entrySet()){
            Log.d("MYAPP", entry.getValue().getPageID());
            entry.getValue().printNotes();
        }
    }
}
