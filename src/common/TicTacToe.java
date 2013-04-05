package common;

import common.exceptions.DuplicateNickException;
import common.exceptions.ReservedBotNickException;
import common.model.Board;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface TicTacToe extends Remote {
    void register(ClientListener clientListener, String nick) throws RemoteException, DuplicateNickException, ReservedBotNickException;

    List<String> getFreeNicks() throws RemoteException;

    void beginPlayingWithBot(String nick, Board board) throws RemoteException;

    Board makeBotMove(String nick, Board board) throws RemoteException;

    void challengeOpponent(String me, String opponent) throws RemoteException;

    void beginGame(String nick, String chosenOpponent) throws RemoteException;

    void uncheck(String nick) throws RemoteException;
}
