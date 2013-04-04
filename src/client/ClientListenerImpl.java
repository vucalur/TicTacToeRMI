package client;

import common.ClientListener;
import common.TicTacToe;
import common.model.Board;

import java.rmi.RemoteException;

public class ClientListenerImpl implements ClientListener {
    //    private final Scanner in = new Scanner(System.in);
    private final TicTacToe stubToServer;
    private final String myNick;
    private String opponent = null;
    private Board board = null;

    public ClientListenerImpl(TicTacToe stubToServer, String nick) {
        this.stubToServer = stubToServer;
        this.myNick = nick;
    }

    @Override
    public void notifyGameStarted(String opponent) throws RemoteException {
        this.opponent = opponent;
    }

    @Override
    public synchronized void notifyYourMove(Board board) throws RemoteException {
        this.board = board;
        notify();
    }

    public String getNewOpponent() {
        return opponent;
    }
}
