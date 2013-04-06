package common;

import common.exceptions.DuplicateNickException;
import common.exceptions.ReservedBotNickException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface TicTacToe extends Remote {
    void register(ClientListener clientListener, String nick) throws RemoteException, DuplicateNickException, ReservedBotNickException;

    void unregister(String nick) throws RemoteException;

    List<String> getFreeNicks() throws RemoteException;

    void playWithOtherPlayer(String nick, String chosenOpponent) throws RemoteException;

    void playWithBot(String nick) throws RemoteException;
}
