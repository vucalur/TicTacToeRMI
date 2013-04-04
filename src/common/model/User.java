/* Copyright 2012 Sabre Holdings */
package common.model;

import common.ClientListener;

import java.io.Serializable;

public class User implements Serializable {
    private final String nick;
    private User playsWith;
    private final ClientListener listener;

    public static final User BOT = new User("bot", null);

    public User(String nick, ClientListener listener) {
        this.nick = nick;
        this.listener = listener;
        this.playsWith = null;
    }

    public String getNick() {
        return nick;
    }

    public User getPlaysWith() {
        return playsWith;
    }

    public void setPlaysWith(User playsWith) {
        this.playsWith = playsWith;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof User)) {
            return false;
        }
        return ((User) obj).nick.equals(this.nick);
    }

    @Override
    public int hashCode() {
        return this.nick.hashCode();
    }

    public ClientListener getListener() {
        return listener;
    }
}
