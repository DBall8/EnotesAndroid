package edudcball.wpi.users.enotesandroid;

/**
 * Simple class for passing event handling code to another class
 * @param <T> the type of object given in the event response
 */
public abstract class EventHandler<T> {
    public abstract void handle(T event);
}
