package edudcball.wpi.users.enotesandroid.data;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edudcball.wpi.users.enotesandroid.data.classes.Sortable;
import edudcball.wpi.users.enotesandroid.noteDataTypes.NoteLookupTable;
import edudcball.wpi.users.enotesandroid.observerPattern.IObserver;
import edudcball.wpi.users.enotesandroid.observerPattern.Observable;

public class SortedList<T extends Sortable> extends Observable implements IObserver {

    public enum SortMode{
        LATEST,
        COLOR,
        ALPHA,
        INDEX
    }

    private List<T> items = new ArrayList<>();
    private List<String> titles = new ArrayList<>();
    private SortMode sortMode = SortMode.LATEST;

    public SortedList(){}

    public T getItem(int index){
        return items.get(index);
    }

    public T getItem(String id){
        for (T item: items){
            if (item.getId().equals(id)){
                return item;
            }
        }

        System.err.format("ERROR: Failed to find item with ID: %s\n", id);
        return null;
    }

    public int getItemIndex(String id){
        for (int i=0; i<items.size(); i++){
            if (items.get(i).getId().equals(id)){
                return i;
            }
        }

        System.err.format("ERROR: Failed to find item with ID: %s\n", id);
        return -1;
    }

    public void add(T item)
    {
        items.add(item);
        titles.add(item.getDisplayTitle());

        reSort();

        item.subscribe(this);
        notifyObservers(null);
    }

    public void remove(int index){
        items.get(index).unSubscribe(this);
        items.remove(index);
        titles.remove(index);

        reSort();

        notifyObservers(null);
    }

    public void remove(String id){
        for (int i=0; i<items.size(); i++){
            if (items.get(i).getId().equals(id)){
                items.remove(i);
                titles.remove(i);

                reSort();

                notifyObservers(null);
                return;
            }
        }

        System.err.format("ERROR: Failed to find and delete item with ID: %s\n", id);
    }

    public void clear(){
        titles.clear();
        items.clear();
    }

    public boolean containsItemWithId(String id){
        for (int i=0; i<items.size(); i++){
            if (items.get(i).getId().equals(id)){
                return true;
            }
        }

        return false;
    }

    public int size(){ return items.size(); }

    public List<String> getTitleList(){ return titles; }

    private void reSort(){

        // Sort items, and only recalculate titles if the order has changed
        boolean hasChanged = sort();
        if (hasChanged) {
            titles.clear();
            for (Sortable item : items) {
                titles.add(item.getDisplayTitle());
            }
        }

        notifyObservers(null);
    }

    @Override
    public void update(String id) {

        // See if any resorting is needed
        reSort();

        // See if the displayed title has changed
        int itemIndex = getItemIndex(id);
        if (itemIndex >=0){
            String oldTitle = titles.get(itemIndex);
            String currentTitle = items.get(itemIndex).getDisplayTitle();

            if(!oldTitle.equals(currentTitle)){
                titles.set(itemIndex, currentTitle);
            }
        }
    }

    public void setSortMode(SortMode sortMode){
        if (this.sortMode != sortMode){
            this.sortMode = sortMode;
            reSort();
        }
    }
    public SortMode getSortMode(){ return sortMode; }

    private boolean sort(){

        boolean hasChanged = false;
        for (int i=items.size()-1; i>=1; i--){
            for (int j=0; j<i; j++) {

                T item1 = items.get(j);
                T item2 = items.get(j + 1);

                if (needsSwapped(item1, item2)) {
                    hasChanged = true;
                    items.set(j, item2);
                    items.set(j + 1, item1);
                }
            }
        }

        return hasChanged;
    }

    private boolean needsSwapped(T item1, T item2){
        switch (sortMode){
            case LATEST:
                return item1.getIndex() < item2.getIndex();
            case INDEX:
                return item1.getIndex() > item2.getIndex();
            case ALPHA:
                return !isFirstAlphabetically(item1.getDisplayTitle(), item2.getDisplayTitle());
            case COLOR:
                int color1Int = NoteLookupTable.getColorOrder(NoteLookupTable.getColorFromJson(item1.getColors()));
                int color2Int = NoteLookupTable.getColorOrder(NoteLookupTable.getColorFromJson(item2.getColors()));
                return color1Int > color2Int;
            default:
                return false;
        }
    }

    private boolean isFirstAlphabetically(String s1, String s2){
        int shortestWordLength = Math.min(s1.length(), s2.length());

        // Compare each character at the same position
        for (int i=0; i<shortestWordLength; i++){

            // If they are not the same character, return which is first alphabetically
            // Otherwise, move to the next character
            char c1 = Character.toLowerCase(s1.charAt(i));
            char c2 = Character.toLowerCase(s2.charAt(i));
            if (c1 != c2){
                return c1 < c2;
            }
        }

        // If all characters up to the shortest's length are the same, the shortest one is first
        return s1.length() < s2.length();
    }
}
