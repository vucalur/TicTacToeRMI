package common.exceptions;

public class DuplicateNickException extends Exception {
    public DuplicateNickException() {
        super("nick already in use");
    }
}
