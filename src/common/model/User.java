package common.model;

import common.ClientListener;

import java.io.Serializable;

public class User implements Serializable {
    private final String nick;
    private final ClientListener listener;

    public User(String nick, ClientListener listener) {
        this.nick = nick;
        this.listener = listener;
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
