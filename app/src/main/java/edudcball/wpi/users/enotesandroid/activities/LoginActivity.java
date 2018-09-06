package edudcball.wpi.users.enotesandroid.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.AsyncTasks.userTasks.CreateUserTask;
import edudcball.wpi.users.enotesandroid.AsyncTasks.userTasks.LoginTask;
import edudcball.wpi.users.enotesandroid.NoteManager;
import edudcball.wpi.users.enotesandroid.R;
import edudcball.wpi.users.enotesandroid.Settings;
import edudcball.wpi.users.enotesandroid.noteDataTypes.NoteLookupTable;

/**
 * Class for running the Login screen of the application
 */

public class LoginActivity extends AppCompatActivity {

    // Screen fields and buttons
    private EditText usernameField;
    private EditText passwordField;
    private EditText passwordConfirm;
    private Button loginButton;
    private TextView messageText;
    private TextView otherLoginModeText;

    // True when in new user mode, false when in log in mode
    private Boolean newUser = false;

    /**
     * Runs when the login activity is first opened
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // load layout
        setContentView(R.layout.activity_login);

        // find all the views
        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        passwordConfirm = findViewById(R.id.passwordConfirmation);
        loginButton = findViewById(R.id.loginButton);
        messageText = findViewById(R.id.messageText);
        otherLoginModeText = findViewById(R.id.switchLoginType);

        // Set up button actions

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLoginClick();
            }
        });

        // this makes clicking on the text below the login fields switch the activity between
        // new user mode and login mode, where new user mode is for creating a new account
        // and login mode is for logging in to an existing account
        otherLoginModeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchNewUser();
            }
        });
    }

    /**
     * Gets called each time the user returns to the login screen
     */
    @Override
    protected void onResume(){
        super.onResume();

        // remove keyboard at the start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Wipe any saved session info
        NoteManager.resetCookies();
        SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
        sp.edit().putString("session", null).commit();

        // Get any error message text that might have been provided if an error caused te
        String error = getIntent().getStringExtra("error");
        if(error != null){
            displayError(error);
        }
        else{
            hideError();
        }

        // if in newUser mode, make sure the UI shows it
        if(newUser){
            switchNewUser();
        }

        // make sure the login button is enabled
        loginButton.setEnabled(true);
    }

    /**
     * Method that is called when the user clicks the login/create account button
     */
    private void handleLoginClick(){
        // get username and password field values
        String usernameAttempt = usernameField.getText().toString();
        String passwordAttempt = passwordField.getText().toString();

        // ensure the fields aren't blank
        if(usernameAttempt.equals("")){
            displayError("Please enter a username.");
            return;
        }
        if(passwordAttempt.equals("")){
            displayError("Please enter a password.");
            return;
        }


        // if in new user mode
        if(newUser){
            // make sure the confirm field isn't blank
            String passwordConfirmAttempt = passwordConfirm.getText().toString();
            if(passwordConfirmAttempt.equals("")){
                displayError("Please enter confirm your password.");
                return;
            }
            // check that the confirm password matches the given password
            if(!passwordAttempt.equals(passwordConfirmAttempt)){
                displayError("Error: passwords do not match.");
                return;
            }

            // create the new user account
            createNewUser(usernameAttempt, passwordAttempt);
        }
        else{
            // attempt to log in
            login(usernameAttempt, passwordAttempt);
        }
    }

    /**
     * Attempts to log in the user
     * @param usernameAttempt the username the user is attempting to login with
     * @param passwordAttempt the password the user is attempting to login with
     */
    private void login(final String usernameAttempt, final String passwordAttempt){

        // hide any previous error message
        hideError();

        // hide the login button and display that login is in progress
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        // Create a background task that contacts the server for a user session
        new LoginTask(usernameAttempt, passwordAttempt){

            /**
             * CALLBACK - this method is called after the server responds
             * @param result the server's response body as a string
             */
            @Override
            protected void onPostExecute(String result) {

                // return login button to its normal state
                loginButton.setText("Login");
                loginButton.setEnabled(true);

                Log.d("MYAPP", result);

                // if no response, something went wrong, end now
                if(result == null){
                    displayError("Could not connect to server. Please try again later.");
                    return;
                }

                try{
                    // Convert response to a JSON object
                    JSONObject obj = new JSONObject(result);
                    // If successful flag received, save the session id on the phone for next time
                    if(obj.getBoolean("successful")){
                        SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
                        sp.edit().putString("session", NoteManager.getCookies().getCookies().get(0).toString()).commit();

                        if(!obj.isNull("dFont")){
                            Settings.setDefaultFont(NoteLookupTable.getFontFromStr(obj.getString("dFont")));
                        }

                        if(!obj.isNull("dFontSize")){
                            Settings.setDefaultFontSize(obj.getInt("dFontSize"));
                        }

                        if(!obj.isNull("dColor")){
                            Settings.setDefaultColor(NoteLookupTable.getColorFromStr(obj.getString("dColor")));
                        }

                        // move to login success method
                        loginSuccess();
                    }
                    else{
                        // If no successful flag, show an error
                        displayError("Login failed. Incorrect username or password.");
                    }
                }
                catch(Exception e){
                    Log.d("MYAPP", "Failed to parse JSON: " + result);
                    displayError("Error when connecting to server.");
                }
            }
        }.execute();
    }

    /**
     * Contacts the server to attempt to create a new user account
     * @param usernameAttempt the username to use for the new account
     * @param passwordAttempt the password to use for the new account
     */
    private void createNewUser(final String usernameAttempt, final String passwordAttempt){

        // clear any previous error
        hideError();

        // Show on the login button that the account is being created
        loginButton.setEnabled(false);
        loginButton.setText("Creating account...");

        // Create a background task that attempts to create the new account
        new CreateUserTask(usernameAttempt, passwordAttempt){

            /**
             * CALLBACK - gets called after the server responds
             * @param result the server's response body as a string
             */
            @Override
            protected void onPostExecute(String result) {

                // return login button back to normal
                loginButton.setText("Create Account");
                loginButton.setEnabled(true);

                // if no response, something went wrong, end early
                if(result == null){
                    displayError("Could not connect to server. Please try again later.");
                    return;
                }

                try{
                    // convert response to a JSON object
                    JSONObject obj = new JSONObject(result);
                    // If server indicates that the user did not already exist, then success!!
                    if(!obj.getBoolean("userAlreadyExists")){
                        // save user session on phone for next time
                        SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
                        sp.edit().putString("session", NoteManager.getCookies().getCookies().get(0).toString()).commit();

                        // move to login success method
                        loginSuccess();
                    }
                    else{
                        // Display an error showing the user already exists
                        displayError("User already exists, please choose a different username.");
                    }
                }
                catch(Exception e){
                    Log.d("MYAPP", "Failed to parse JSON");
                }
            }
        }.execute();
    }

    /**
     * Moves to the main activity once successfully logged in
     */
    private void loginSuccess(){
        // start main activity
        //startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /**
     * Switches between new user and login moves
     */
    private void switchNewUser(){
        // hide any previous error message
        hideError();

        // If in new user mode, switch to login mode
        if(newUser){
            passwordConfirm.setVisibility(View.GONE);
            loginButton.setText("Login");
            otherLoginModeText.setText(R.string.signUpSwitch);
            newUser = false;
        }
        // If in login mode, switch to new user mode
        else{
            passwordConfirm.setVisibility(View.VISIBLE);
            loginButton.setText("Create Account");
            otherLoginModeText.setText(R.string.loginSwitch);
            newUser = true;
        }
    }

    /**
     * Displays an error message
     * @param errorText the message to display
     */
    private void displayError(String errorText){
        messageText.setText(errorText);
        messageText.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the error message
     */
    private void hideError(){
        messageText.setVisibility(View.GONE);
    }
}
