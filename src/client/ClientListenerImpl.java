package client;

import common.ClientListener;
import common.TicTacToe;
import common.enums.Sign;
import common.exceptions.BoardFieldNotEmptyException;
import common.model.Board;

import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.Set;

public class ClientListenerImpl implements ClientListener {
    //    private final Scanner in = new Scanner(System.in);
    private final TicTacToe stubToServer;
    private final String myNick;

    public ClientListenerImpl(TicTacToe stubToServer, String nick) {
        this.stubToServer = stubToServer;
        this.myNick = nick;
    }

    // TODO : remove
    @Override
    public void triggerChooseMode(Set<String> freeNicks) throws RemoteException {
        System.out.println("Available players:");
        for (String nick : freeNicks) {
            System.out.println("\t" + nick);
        }
        System.out.println("choose game mode: 1 - await in queue, 2 - join other player, 3 - play with AI");
        Scanner in = new Scanner(System.in);
        chosen:
        while (true) {
            System.out.println(">>>");
            int choice = in.nextInt();
            switch (choice) {
                case 1:
                    System.out.println("You will be notified when someone chooses to play with you");
                    break chosen;
                case 2:
                    System.out.println("Please choose your partner from the list above");
                    String nick;
                    while (true) {
                        System.out.println(">>>");
                        nick = in.nextLine();
                        if (!freeNicks.contains(nick)) {
                            System.out.println("Wrong choice, try again");
                        } else {
                            break;
                        }
                    }
                    // TODO : verify on server and begin game
                    break chosen;
                case 3:
                    // TODO
                    System.out.println("Wrong choice, try cdcdcd 33333");
                    break chosen;
                default:
                    System.out.println("Wrong choice, try again");
            }
        }
    }

    @Override
    public Board  makeMoveAgainstBot(Board board) throws RemoteException {
        if (!board.someoneHasWon()) {
            System.out.println("=========================================================");
            System.out.println("board:\n" + board.getView());
            System.out.println("your move ...");
            int i, j;
            Scanner in = new Scanner(System.in);
            while (true) {
                System.out.print(">> ");
                i = in.nextInt();
                j = in.nextInt();

                try {
                    board.place(Sign.NON_BOT_SIGN, i, j);
                    break;
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Wrong indices. try again");
                } catch (BoardFieldNotEmptyException e) {
                    System.out.println("field already taken. try again");
                }
            }
            // stubToServer.makeBotMove(myNick, board);
        } else {
            System.out.println("You " + ((board.whoWins() == Sign.NON_BOT_SIGN) ? " win !" : "lose"));
        }
        return board;
    }
}
