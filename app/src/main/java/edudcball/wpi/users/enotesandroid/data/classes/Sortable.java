package edudcball.wpi.users.enotesandroid.data.classes;

import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.observerPattern.Observable;

public abstract class Sortable extends Observable {
    public abstract String getDisplayTitle();
    public abstract String getId();
    public abstract int getIndex();
    public abstract JSONObject getColors();
}
