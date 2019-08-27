package edudcball.wpi.users.enotesandroid.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.Callback;
import edudcball.wpi.users.enotesandroid.Old.noteDataTypes.NoteLookupTable;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.userTasks.CreateUserTask;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.userTasks.LoginTask;
import edudcball.wpi.users.enotesandroid.R;
import edudcball.wpi.users.enotesandroid.Settings;
import edudcball.wpi.users.enotesandroid.data.UserManager;

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

    private UserManager userManager;

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

        passwordField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if((i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_NEXT ) && !newUser){
                    handleLoginClick();
                }

                return false;
            }
        });

        passwordConfirm.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if((i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_NEXT )&& newUser){
                    handleLoginClick();
                }

                return false;
            }
        });

        userManager = UserManager.getInstance();
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
        resetStoredLoginInfo();

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

        SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
        sp.edit().putString("username", usernameAttempt);
        sp.edit().putString("password", usernameAttempt);
        sp.edit().commit();

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

            // clear any previous error
            hideError();

            // Show on the login button that the account is being created
            loginButton.setEnabled(false);
            loginButton.setText("Creating account...");

            // create the new user account
            new CreateUserTask(usernameAttempt, passwordAttempt, new Callback<String>() {
                @Override
                public void run(String param) {
                    handleNewUserResponse(param);
                }
            }).execute();
        }
        else{
            // hide any previous error message
            hideError();

            // hide the login button and display that login is in progress
            loginButton.setEnabled(false);
            loginButton.setText("Logging in...");

            // attempt to log in
            new LoginTask(usernameAttempt, passwordAttempt, new Callback<String>(){
                @Override
                public void run(String param){
                    handleLoginResponse(param);
                }
            }).execute();
        }
    }

    private void handleLoginResponse(String response){

        // return login button to its normal state
        loginButton.setText("Login");
        loginButton.setEnabled(true);

        Log.d("MYAPP", "RESULT: " + response);

        // if no response, something went wrong, end now
        if (response == null) {
            displayError("Could not connect to server. Please try again later.");
            return;
        }

        try {
            // Convert response to a JSON object
            JSONObject obj = new JSONObject(response);
            // If successful flag received, save the session id on the phone for next time
            if (obj.getBoolean("successful")) {
                if (!obj.isNull("dfont")) {
                    Settings.setDefaultFont(NoteLookupTable.getFontFromStr(obj.getString("dfont")));
                }

                if (!obj.isNull("dfontsize")) {
                    Settings.setDefaultFontSize(obj.getInt("dfontsize"));
                }

                if (!obj.isNull("dcolor")) {
                    Settings.setDefaultColor(NoteLookupTable.getColorFromStr(obj.getString("dcolor")));
                }

                // Login successful, finish this activity
                finish();
            } else {
                // If no successful flag, show an error
                displayError("Login failed. Incorrect username or password.");
                resetStoredLoginInfo();
            }
        } catch (Exception e) {
            Log.d("MYAPP", "Failed to parse JSON: " + response);
            displayError("Error when connecting to server.");
        }
    }

    /**
     * Contacts the server to attempt to create a new user account
     */
    private void handleNewUserResponse(String response){

        // return login button back to normal
        loginButton.setText("Create Account");
        loginButton.setEnabled(true);

        // if no response, something went wrong, end early
        if (response == null) {
            displayError("Could not connect to server. Please try again later.");
            return;
        }

        try {
            // convert response to a JSON object
            JSONObject obj = new JSONObject(response);
            // If server indicates that the user did not already exist, then success!!
            if (!obj.getBoolean("userAlreadyExists")) {
                // User creation successful, return to main activity
                finish();
            } else {
                // Display an error showing the user already exists
                displayError("User already exists, please choose a different username.");
                resetStoredLoginInfo();
            }
        } catch (Exception e) {
            Log.d("MYAPP", "Failed to parse JSON");
        }
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

    private void resetStoredLoginInfo(){
        SharedPreferences sp = getSharedPreferences("Login", MODE_PRIVATE);
        sp.edit().putString("username", null);
        sp.edit().putString("password", null);
        sp.edit().commit();
    }
}
