package common;

import common.model.Board;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientListener extends Remote {
    void notifyGameStarted(String opponent) throws RemoteException;

    void notifyYourMove(Board board) throws RemoteException;
}
