package edudcball.wpi.users.enotesandroid.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import edudcball.wpi.users.enotesandroid.Callback;
import edudcball.wpi.users.enotesandroid.data.UserManager;

public abstract class EnotesActivity extends AppCompatActivity {
    private static MainActivity mainActivity;
    private static String errorMessage;
    private static boolean hasErrored = false;

    protected void launchActivity(Context context, Class classToStart){
        startActivity(new Intent(context, classToStart));
    }

    public void showErrorAndLogout(String errorMessage){
        this.errorMessage = errorMessage;
        this.hasErrored = true;
        UserManager.getInstance().logOut(mainActivity.getApplicationContext(), new Callback<String>() {
            @Override
            public void run(String param) {
                // Not needed
            }
        });
    }

    protected void setMainActivity(MainActivity activity){
        this.mainActivity = activity;
    }

    protected String getErrorMessage(){ return errorMessage; }
    protected boolean hasErroredOccurred(){ return hasErrored; }
    protected void clearError(){ hasErrored = false; }

    @Override
    public void onStop(){
        super.onStop();
        UserManager.getInstance().getPageManager().update(new Callback<Boolean>() {
            @Override
            public void run(Boolean param) {
                // Nothing to do yet
            }
        });
    }
}
