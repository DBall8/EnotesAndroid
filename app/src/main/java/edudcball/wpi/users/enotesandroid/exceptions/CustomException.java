package edudcball.wpi.users.enotesandroid.exceptions;

public class CustomException extends Exception{

    private String message;
    private String errorMessage;

    public CustomException(String message, String errorMessage){
        this.message = message;
        this.errorMessage = errorMessage;
    }

    public String getMessage() { return message; }
    public String getErrorMessage() { return errorMessage; }
}
