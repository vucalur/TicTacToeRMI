package server;

import common.PropertiesKeys;
import common.TicTacToe;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class TicTacToeServer {
    static TicTacToe impl;

    public static void main(String[] args) {
        for (String key : new String[]{PropertiesKeys.HOST, PropertiesKeys.PORT, PropertiesKeys.OBJECT_NAME}) {
            if (System.getProperty(key) == null) {
                System.out.println("Property: " + key + " must be defined");
                System.exit(1);
            }
        }
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            LocateRegistry.createRegistry(Integer.parseInt(System.getProperty(PropertiesKeys.PORT)));
            impl = new TicTacToeImpl();
//            TicTacToe skeleton =
//                    (TicTacToe) UnicastRemoteObject.exportObject(impl, 0); // It is common to use the value zero, which specifies the use of an anonymous port. The actual port will then be chosen at runtime by RMI or the underlying operating system.
            Naming.rebind("rmi://" + System.getProperty(PropertiesKeys.HOST) + ":" + System.getProperty(PropertiesKeys.PORT) + "/" + System.getProperty(PropertiesKeys.OBJECT_NAME), impl);
            System.out.println("TicTacToeServer bound");
        } catch (Exception e) {
            System.err.println("TicTacToeServer exception:");
            e.printStackTrace();
        }
    }
}
