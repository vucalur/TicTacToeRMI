package client;

import common.ClientListener;
import common.PropertiesKeys;
import common.TicTacToe;
import common.enums.Sign;
import common.exceptions.BoardFieldNotEmptyException;
import common.exceptions.DuplicateNickException;
import common.exceptions.ReservedBotNickException;
import common.model.Board;

import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        for (String key : new String[]{PropertiesKeys.HOST, PropertiesKeys.PORT, PropertiesKeys.OBJECT_NAME, PropertiesKeys.NICK}) {
            if (System.getProperty(key) == null) {
                System.out.println("Property: " + key + " must be defined");
                System.exit(1);
            }
        }
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            final String nick = System.getProperty(PropertiesKeys.NICK);

            final TicTacToe serverStub = (TicTacToe) Naming.lookup("rmi://" + System.getProperty(PropertiesKeys.HOST) + ":" + System.getProperty(PropertiesKeys.PORT) + "/" + System.getProperty(PropertiesKeys.OBJECT_NAME));
            final ClientListenerImpl listener = new ClientListenerImpl(serverStub, nick);
            ClientListener meStub = (ClientListener) UnicastRemoteObject.exportObject(listener, 0);
            serverStub.register(meStub, nick);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    System.out.println("\ncleanup & quit...");
                    try {
                        serverStub.logout(nick);
                        UnicastRemoteObject.unexportObject(listener, true);
                    } catch (NoSuchObjectException e) {
                        System.out.println("server already terminated");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });

            System.out.println("Available commands: list, play (bot | <user>)");
            Scanner in = new Scanner(System.in);
            while (true) {
                String opponentObtainedPassively = listener.getNewOpponent();
                if (opponentObtainedPassively != null) {
                    System.out.println("You have been challlenged by " + opponentObtainedPassively + " !");
                    beginGame(false, nick, opponentObtainedPassively, serverStub, listener);
                    return;
                }

                System.out.print(">>> ");
                String line = in.next();
                switch (line) {
                    case "list":
                        System.out.println("Available players:");
                        for (String n : serverStub.getFreeNicks())
                            System.out.println("\t" + n);
                        break;
                    case "play":
                        String chosenOpponent = in.next();
                        if (chosenOpponent.equals("bot")) {
                            Board board = new Board();
                            serverStub.beginPlayingWithBot(nick, board);

                            while (!board.someoneHasWon()) {
                                if (board.whoseMove() == Sign.BOT_SIGN)
                                    board = serverStub.makeBotMove(nick, board);
                                if (board.someoneHasWon())
                                    break;
                                System.out.println("=========================================================");
                                System.out.println("board:\n" + board.getView());
                                System.out.println("your move ...");
                                int i, j;
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
                            }
                            System.out.println("You " + ((board.whoWins() == Sign.NON_BOT_SIGN) ? "win !" : "lose"));
                            System.out.println("board:\n" + board.getView());
                        } else if (!serverStub.getFreeNicks().contains(chosenOpponent)) {
                            System.out.println("player " + chosenOpponent + " no longer available. try again");
                            break;
                        } else {
                            serverStub.challengeOpponent(nick, chosenOpponent);
                            beginGame(true, nick, chosenOpponent, serverStub, listener);
                        }
                        return;
                    default:
                        System.out.println("Unknown command");
                }
            }
        } catch (ReservedBotNickException | DuplicateNickException e) {    // TODO : more exceptions
            System.err.println(e.getLocalizedMessage());
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Client exception:");
            throw new RuntimeException(e);
        }
    }

    private static void beginGame(boolean iBeganTheGame, String myNick, String opponent, TicTacToe serverStub, ClientListenerImpl listener) {
        Scanner in = new Scanner(System.in);
        Board board = null;
        if (iBeganTheGame) {
            board = new Board();
        } else {
            synchronized ()
        }

        final Sign mySign = iBeganTheGame ? Sign.NON_BOT_SIGN : Sign.BOT_SIGN;
        while (!board.someoneHasWon()) {
            if (board.whoseMove() == mySign) {
                System.out.println("=========================================================");
                System.out.println("board:\n" + board.getView());
                System.out.println("your move ...");
                int i, j;
                while (true) {
                    System.out.print(">> ");
                    i = in.nextInt();
                    j = in.nextInt();

                    try {
                        board.place(mySign, i, j);
                        break;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("Wrong indices. try again");
                    } catch (BoardFieldNotEmptyException e) {
                        System.out.println("field already taken. try again");
                    }
                }

                if (board.someoneHasWon())
                    break;
            } else {
                try {
                    serverStub.notifyOpponentYourMove(opponent, board);
                } catch (RemoteException e) {
                    System.out.println("Problem connecting opponent. quiting");
                    return;
                }
            }

        }
        System.out.println("You " + ((board.whoWins() == Sign.NON_BOT_SIGN) ? "win !" : "lose"));
        System.out.println("board:\n" + board.getView());
    }
}
