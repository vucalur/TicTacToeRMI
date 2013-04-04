package client;

import common.ClientListener;
import common.PropertiesKeys;
import common.TicTacToe;
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
                    } catch(NoSuchObjectException e) {
                        System.out.println("server already terminated");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });

            System.out.println("Available commands: list, play (bot | <user>)");
            Scanner in = new Scanner(System.in);
            chose:
            while (true) {
                System.out.print(">>> ");
                String line = in.next();
                switch (line) {
//                    case "exit":
//                        serverStub.logout(nick);
//                        System.exit(0);
                    case "list":
                        System.out.println("Available players:");
                        for (String n : serverStub.getFreeNicks()) {
                            System.out.println("\t" + n);
                        }
                        break;
                    case "play":
                        String opponent = in.next();
                        if (opponent.equals("bot")) {
                            Board board = new Board();
                            serverStub.beginPlayingWithBot(nick, board);
                        } else {
                            // TODO
                        }
                        break chose;
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
}
