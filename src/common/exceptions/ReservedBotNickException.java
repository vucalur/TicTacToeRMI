package common.exceptions;

public class ReservedBotNickException extends Exception {
    public ReservedBotNickException() {
        super("nick \"bot\" is reserved for special use");
    }
}
