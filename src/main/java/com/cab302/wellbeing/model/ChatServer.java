package com.cab302.wellbeing.model;//Source:
//  Creating a simple Chat Client/Server Solution 
//  http://pirate.shu.edu/~wachsmut/Teaching/CSAS2214/Virtual/Lectures/chat-client-server.html


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class is a thread for the chat server.
 * It provides methods to open, close, and run the server.
 */
public class ChatServer implements Runnable {
    private ChatServerThread[] clients = new ChatServerThread[50]; // An array of client threads
    private ServerSocket server = null; // The server socket
    private Thread thread = null; // The server thread
    private int clientCount = 0; // The number of clients
    private volatile boolean running = true; // The running state of the server

    /**
     * This method is used to initialize the chat server.
     * @param port - the port for the server
     */
    public ChatServer(int port) {
        try {
            System.out.println("Binding to port " + port + ", please wait  ...");
            server = new ServerSocket(port);
            System.out.println("Server started: " + server);
        } catch (IOException ioe) {
            System.out.println("Cannot bind to port " + port + ": " + ioe.getMessage());
        }
    }

    /**
     * This method is used to start the server thread.
     */
    public void run() {
        while (running) {
            try {
                System.out.println("Waiting for a client ...");
                addThread(server.accept());
            } catch (IOException ioe) {
                if (running) {
                    System.out.println("Server accept error: " + ioe);
                }
                stop();
            }
        }
    }

    /**
     * This method is used to stop the server thread.
     */
    public void stop() {
        running = false;
        if (thread != null) {
            try {
                if (server != null && !server.isClosed()) {
                    server.close();
                }
            } catch (IOException ioe) {
                System.out.println("Error closing server socket: " + ioe.getMessage());
            }
            thread = null;
        }
    }

    /**
     * This method is used to find the Client.
     * @param ID - the client socket
     */
    private synchronized int findClient(int ID) {
        for (int i = 0; i < clientCount; i++) {
            if (clients[i].getID() == ID) {
                return i;
            }
        }
        return -1;
    }

    /**
     * This method is used to synchronize the server thread.
     * @param ID - the client ID
     * @param input - the input from the client
     */
    public synchronized void handle(int ID, String input) {
        if (input.equals(".bye")) {
            clients[findClient(ID)].send(".bye");
            remove(ID);
        } else {
            for (int i = 0; i < clientCount; i++) {
                clients[i].send(ID + ": " + input);
            }
        }
    }

    /**
     * This method is used to remove the server thread.
     * @param ID - the client ID
     */
    public synchronized void remove(int ID) {
        int pos = findClient(ID);
        if (pos >= 0) {
            ChatServerThread toTerminate = clients[pos];
            System.out.println("Removing client thread " + ID + " at " + pos);
            if (pos < clientCount - 1) {
                for (int i = pos + 1; i < clientCount; i++) {
                    clients[i - 1] = clients[i];
                }
            }
            clientCount--;
            toTerminate.connClose(); // No need for try-catch here since close() no longer throws IOException
        }
    }

    /**
     * This method is used to add Thread.
     * @param socket - the client socket
     */
    private void addThread(Socket socket) {
        if (clientCount < clients.length) {
            System.out.println("Client accepted: " + socket);
            clients[clientCount] = new ChatServerThread(this, socket);
            try {
                clients[clientCount].open();
                clients[clientCount].start();
                clientCount++;
            } catch (IOException ioe) {
                System.out.println("Error opening thread: " + ioe);
            }
        } else {
            System.out.println("Client refused: maximum " + clients.length + " reached.");
        }
    }
}

