package common.enums;

public enum Sign {
    CIRCLE('O'), CROSS('X'), EMPTY(' ');
    public static final Sign BOT_SIGN = CIRCLE;
    public static final Sign NON_BOT_SIGN = CROSS;
    public final char text;

    private Sign(char text) {
        this.text = text;
    }
}
