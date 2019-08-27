package edudcball.wpi.users.enotesandroid.connection;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;

public class ConnectionManager {

    private SocketConnection socketConnection;
    private CookieManager cookies = new CookieManager(); // manages cookies

    public ConnectionManager(String username){
        socketConnection = new SocketConnection(username);
        cookies = new CookieManager();
    }

    public void addCookie(String cookie){
        cookies.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
    }

    public CookieStore getCookies(){ return cookies.getCookieStore(); }


}
