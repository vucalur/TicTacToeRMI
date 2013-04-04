package common.exceptions;

public class BoardFieldNotEmptyException extends Exception {
    public BoardFieldNotEmptyException() {
        super("field not empty! invalid move");
    }
}
