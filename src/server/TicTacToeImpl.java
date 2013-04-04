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
    public void beginPlayingWithBot(final String nick, final Board board) throws RemoteException {
        freeNicks.remove(nick);
        loggedUsers.get(nick).setPlaysWith(User.BOT);
//        final Object lock = new Object();
        ClientListener clientListenerNonFinal;
        synchronized (this) {
            clientListenerNonFinal = loggedUsers.get(nick).getListener();
        }
        final ClientListener clientListener = clientListenerNonFinal;
        Runnable executor = new Runnable() {
            @Override
            public void run() {
                Board localBoard = board;
                while (true) {
                    try {
                        localBoard = clientListener.makeMoveAgainstBot(localBoard);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        return;
                    }
                    if (localBoard.someoneHasWon()) {
                        freeNicks.add(nick);
                        return;
                    }
                    System.out.println("111");
                    localBoard.makeRandomBotMove();
                    System.out.println("222");
                }
            }
        };
//        locks.put(nick, lock);
        executors.put(nick, executor);
        executorService.submit(executor);
//        new Thread(executor).start();
    }

    // TODO: remove if unused
    @Override
    public void markAvailable(String nick) throws RemoteException {
        freeNicks.add(nick);
    }

    @Override
    public void makeBotMove(String nick, Board board) throws RemoteException {
//        Object lock = locks.get(nick);
//        synchronized (lock) {
//            //TODO
//        }
    }

    private synchronized void remove(String nick) {
        loggedUsers.remove(nick);
        freeNicks.remove(nick);
    }
}
