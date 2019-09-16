package edudcball.wpi.users.enotesandroid.data;

import java.util.ArrayList;
import java.util.List;

import edudcball.wpi.users.enotesandroid.data.classes.Sortable;
import edudcball.wpi.users.enotesandroid.observerPattern.Observable;

public class SortedList<T extends Sortable> extends Observable {

    private List<T> items = new ArrayList<>();
    private List<String> titles = new ArrayList<>();

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

        notifyObservers();
    }

    public void remove(int index){
        items.remove(index);
        items.remove(index);

        notifyObservers();
    }

    public void remove(String id){
        for (int i=0; i<items.size(); i++){
            if (items.get(i).getId().equals(id)){
                items.remove(i);
                titles.remove(i);

                notifyObservers();
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
        // TODO fill in

        notifyObservers();
    }
}
