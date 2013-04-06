package client;

import common.ClientListener;
import common.PropertiesKeys;
import common.TicTacToe;
import common.exceptions.DuplicateNickException;
import common.exceptions.ReservedBotNickException;
import server.User;

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
                        serverStub.unregister(nick);
                        UnicastRemoteObject.unexportObject(listener, true);
                    } catch (RemoteException e) {
                        System.out.println("server already terminated");
                    }
                }
            });

            System.out.println("Available commands: await, list, play (" + User.BOT_NICK + " | <user>)");
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
                        if (chosenOpponent.equals(User.BOT_NICK) || chosenOpponent.equals(nick)) {
                            System.out.println("Starting playing with bot");
                            serverStub.playWithBot(nick);
                        } else if (!serverStub.getFreeNicks().contains(chosenOpponent)) {
                            System.out.println("player " + chosenOpponent + " no longer available. try again");
                            break;
                        } else {
                            serverStub.playWithOtherPlayer(nick, chosenOpponent);
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
