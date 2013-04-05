package server;

import common.ClientListener;
import common.TicTacToe;
import common.enums.Sign;
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

public class TicTacToeImpl extends UnicastRemoteObject implements TicTacToe {
    private Map<String, User> loggedUsers = new HashMap<>();
    private List<String> freeNicks = new ArrayList<>(); // currently not playing


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
    public synchronized void beginPlayingWithBot(final String nick, final Board board) throws RemoteException {
        freeNicks.remove(nick);
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
        User uOpponent = loggedUsers.get(opponent);

        uOpponent.getListener().notifyGameStarted(me);
    }

    @Override
    public void beginGame(String nick, String chosenOpponent) throws RemoteException {
        challengeOpponent(nick, chosenOpponent);
        final ClientListener meListener = loggedUsers.get(nick).getListener();
        final ClientListener oppoListener = loggedUsers.get(chosenOpponent).getListener();
        final Sign meSign = Sign.NON_BOT_SIGN;
        final Sign oppoSign = Sign.BOT_SIGN;

        Thread t = new Thread(new Runnable() {
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
        t.start();
    }

    @Override
    public void uncheck(String nick) throws RemoteException {
        remove(nick);
    }

    private synchronized void remove(String nick) {
        loggedUsers.remove(nick);
        freeNicks.remove(nick);
    }
}
