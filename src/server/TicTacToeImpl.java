package server;

import common.ClientListener;
import common.TicTacToe;
import common.exceptions.DuplicateNickException;
import common.exceptions.ReservedBotNickException;
import common.model.Board;
import common.model.User;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TicTacToeImpl extends UnicastRemoteObject implements TicTacToe {
    private final Map<String, Runnable> executors = new HashMap<>();
    ExecutorService executorService = Executors.newCachedThreadPool();
    private Map<String, User> loggedUsers = new HashMap<>();
    private List<String> freeNicks = new ArrayList<>(); // currently not playing
//    private Map<String, Object> locks = new HashMap<>();


    protected TicTacToeImpl() throws RemoteException {
    }

    @Override
    public void register(ClientListener clientListener, String nick) throws RemoteException, DuplicateNickException, ReservedBotNickException {
        if (nick.equals("bot")) {
            throw new ReservedBotNickException();
        }
        synchronized (this) {
            if (loggedUsers.containsKey(nick)) {
                throw new DuplicateNickException();
            }
            loggedUsers.put(nick, new User(nick, clientListener));
            freeNicks.add(nick);
        }

        System.out.println(nick + " is now connected");
    }

    @Override
    public synchronized void logout(String nick) throws RemoteException {
        if (!freeNicks.contains(nick)) {
            throw new IllegalArgumentException("no such user");
        }
        remove(nick);
        System.out.println(nick + " has left");
    }

    @Override
    public synchronized List<String> getFreeNicks() throws RemoteException {
        List<String> result = new ArrayList<>(freeNicks);
        return result;
    }

    @Override
    public synchronized void beginPlayingWithBot(final String nick, final Board board) throws RemoteException {
        freeNicks.remove(nick);
        loggedUsers.get(nick).setPlaysWith(User.BOT);
    }

    @Override
    public Board makeBotMove(String nick, final Board board) throws RemoteException {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                board.makeRandomBotMove();
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return board;
    }

    @Override
    public synchronized void challengeOpponent(String me, String opponent) throws RemoteException {
        freeNicks.remove(me);
        freeNicks.remove(opponent);
        User uMe = loggedUsers.get(me);
        User uOpponent = loggedUsers.get(opponent);
        uOpponent.setPlaysWith(uMe);
        uMe.setPlaysWith(uOpponent);

        uOpponent.getListener().notifyGameStarted(me);
    }

    @Override
    public void notifyOpponentYourMove(final String opponent, final Board board) throws RemoteException {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                loggedUsers.get(opponent).getListener().notifyYourMove(board);
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private synchronized void remove(String nick) {
        loggedUsers.remove(nick);
        freeNicks.remove(nick);
    }
}
