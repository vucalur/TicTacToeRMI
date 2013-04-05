package common;

import common.enums.Sign;
import common.model.Board;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientListener extends Remote {
    void notifyGameStarted(String opponent) throws RemoteException;

    Board makeMove(Board board, Sign signToUse) throws RemoteException;

    void notifyEndOfGame(Board finalBoard, Sign meSign) throws RemoteException;
}
