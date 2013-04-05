package client;

import common.ClientListener;
import common.enums.Sign;
import common.exceptions.BoardFieldNotEmptyException;
import common.model.Board;

import java.rmi.RemoteException;
import java.util.Scanner;

public class ClientListenerImpl implements ClientListener {

    private String opponent = null;

    @Override
    public synchronized void notifyGameStarted(String opponent) throws RemoteException {
//        System.out.println("You have been challenged by " + opponent + " !"); // already done in Client class, but it's possible to do it here as well
        this.opponent = opponent;
        notify();
    }

    @Override
    public Board makeMove(Board board, Sign signToUse) throws RemoteException {
        Scanner in = new Scanner(System.in);
        System.out.println("=========================================================");
        System.out.println("board:\n" + board.getView());
        System.out.println("your move ...");
        int i, j;
        while (true) {
            System.out.print(">> ");
            i = in.nextInt();
            j = in.nextInt();

            try {
                board.place(signToUse, i, j);
                break;
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Wrong indices. try again");
            } catch (BoardFieldNotEmptyException e) {
                System.out.println("field already taken. try again");
            }
        }
        return board;
    }

    @Override
    public void notifyEndOfGame(Board finalBoard, Sign meSign) {
        System.out.println(finalBoard.getCommunicate(meSign));
        System.out.println("board:\n" + finalBoard.getView());
        opponent = null; // unnecessary
    }

    public String getNewOpponent() {
        return opponent;
    }
}
