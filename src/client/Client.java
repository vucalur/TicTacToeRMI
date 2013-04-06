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
            final ClientListenerImpl listener = new ClientListenerImpl();
            ClientListener meStub = (ClientListener) UnicastRemoteObject.exportObject(listener, 0);
            serverStub.register(meStub, nick);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    System.out.println("\ncleanup & quit...");
                    try {
                        serverStub.uncheck(nick);
                        UnicastRemoteObject.unexportObject(listener, true);
                    } catch (RemoteException e) {
                        System.out.println("server already terminated");
                    }
                }
            });

            System.out.println("Available commands: await, list, play (bot | <user>)");
            Scanner in = new Scanner(System.in);
            consoleLoop:
            while (true) {
                System.out.print(">>> ");
                String line = in.next();

                switch (line) {
                    case "await":
                        synchronized (listener) {
                            while (listener.getNewOpponent() == null) {
                                listener.wait();
                            }
                        }
                        String opponentObtainedPassively = listener.getNewOpponent();
                        if (opponentObtainedPassively != null) {
                            System.out.println("You have been challenged by " + opponentObtainedPassively + " !");
                            break consoleLoop;
                        } else {
                            throw new RuntimeException("damn - synchronization failed");
                        }
                    case "list":
                        System.out.println("Available players:");
                        for (String n : serverStub.getFreeNicks())
                            System.out.println("\t" + n);
                        break;
                    case "play":
                        String chosenOpponent = in.next();
                        if (chosenOpponent.equals("bot") || chosenOpponent.equals(nick)) {
                            System.out.println("Starting playing with bot");
                            Board board = new Board();
                            serverStub.beginPlayingWithBot(nick, board);

                            while (!board.someoneHasWonOrTie()) {
                                if (board.whoseMove() == Sign.BOT_SIGN)
                                    board = serverStub.makeBotMove(nick, board);
                                if (board.someoneHasWonOrTie())
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
                            System.out.println(board.getCommunicate(Sign.NON_BOT_SIGN));
                            System.out.println("board:\n" + board.getView());
                        } else if (!serverStub.getFreeNicks().contains(chosenOpponent)) {
                            System.out.println("player " + chosenOpponent + " no longer available. try again");
                            break;
                        } else {
                            serverStub.beginGame(nick, chosenOpponent);
                        }
                        return;
                    default:
                        System.out.println("Unknown command");
                }
            }
        } catch (ReservedBotNickException | DuplicateNickException e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Client exception:");
            throw new RuntimeException(e);
        }
    }
}
