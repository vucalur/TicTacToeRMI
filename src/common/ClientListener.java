package common;

import common.model.Board;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface ClientListener extends Remote {
    void triggerChooseMode(Set<String> freeNicks) throws RemoteException;

    Board makeMoveAgainstBot(Board board) throws RemoteException;
}
