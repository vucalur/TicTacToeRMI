package server;

import common.ClientListener;
import common.TicTacToe;
import common.enums.Sign;
import common.exceptions.DuplicateNickException;
import common.exceptions.ReservedBotNickException;
import common.model.Board;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TicTacToeImpl extends UnicastRemoteObject implements TicTacToe {
    private final Map<String, User> loggedUsers = new HashMap<>();
    private final List<String> freeNicks = new ArrayList<>(); // currently not playing
    private final ExecutorService executor = Executors.newCachedThreadPool();

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
    public synchronized List<String> getFreeNicks() throws RemoteException {
        List<String> result = new ArrayList<>(freeNicks);
        return result;
    }

    @Override
    public void playWithOtherPlayer(String nick, String chosenOpponent) throws RemoteException {
        synchronized (this) {
            freeNicks.remove(nick);
            freeNicks.remove(chosenOpponent);
        }
        final ClientListener meListener = loggedUsers.get(nick).getListener();
        final ClientListener oppoListener = loggedUsers.get(chosenOpponent).getListener();
        oppoListener.notifyGameStarted(nick);
        final Sign meSign = Sign.NON_BOT_SIGN;
        final Sign oppoSign = Sign.BOT_SIGN;

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Board board = new Board();
                    while (!board.someoneHasWonOrTie()) {
                        board = meListener.makeMove(board, meSign);
                        if (board.someoneHasWonOrTie()) {
                            meListener.notifyEndOfGame(board, meSign);
                            oppoListener.notifyEndOfGame(board, oppoSign);
                            return;
                        }
                        board = oppoListener.makeMove(board, oppoSign);
                    }
                } catch (RemoteException e) {
                    System.err.println("Connection error occured");
                }
            }
        });
    }

    @Override
    public void playWithBot(String nick) throws RemoteException {
        synchronized (this) {
            freeNicks.remove(nick);
        }
        final ClientListener clientListener = loggedUsers.get(nick).getListener();

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Board board = new Board();
                    while (!board.someoneHasWonOrTie()) {
                        board = clientListener.makeMove(board, Sign.NON_BOT_SIGN);
                        if (board.someoneHasWonOrTie()) {
                            clientListener.notifyEndOfGame(board, Sign.NON_BOT_SIGN);
                            return;
                        }
                        board.makeRandomBotMove();
                    }
                } catch (RemoteException e) {
                    System.err.println("Connection error occured");
                }
            }
        });
    }

    @Override
    public void unregister(String nick) throws RemoteException {
        remove(nick);
    }

    private synchronized void remove(String nick) {
        loggedUsers.remove(nick);
        freeNicks.remove(nick);
    }
}
