package Exceptions;

public class NotValidFileException extends SurveyException {
    public NotValidFileException() {
        super("File is not valid");
    }
    
    public NotValidFileException(String message) {
        super(message);
    }
}
