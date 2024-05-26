package com.cab302.wellbeing.model;//Source:
//  Creating a simple Chat Client/Server Solution 
//  http://pirate.shu.edu/~wachsmut/Teaching/CSAS2214/Virtual/Lectures/chat-client-server.html


import java.io.*;
import java.net.Socket;

/**
 * This class is a thread for the chat server.
 * It provides methods to open, close, and run the server.
 */
public class ChatServerThread extends Thread {
    private ChatServer server; // The chat server
    private Socket socket; // The socket for the server
    private int ID = -1; // The ID for the server
    private DataInputStream streamIn; // The input stream for the server
    private DataOutputStream streamOut; // The output stream for the server
    private volatile boolean running = true; // The running state of the server

    /**
     * This method is used to initialize the chat server thread.
     * @param _server - the chat server
     * @param _socket - the socket for the server
     */
    public ChatServerThread(ChatServer _server, Socket _socket) {
        super();
        server = _server;
        socket = _socket;
        ID = socket.getPort();
    }

    /**
     * This method is used to send a message to the server.
     * @param msg - the message to send
     */
    public void send(String msg) {
        try {
            streamOut.writeUTF(msg);
            streamOut.flush();
        } catch (IOException ioe) {
            System.out.println(ID + " ERROR sending: " + ioe.getMessage());
            server.remove(ID);
            connClose();
        }
    }

    /**
     * This method is used to get the ID of the server.
     * @return the ID of the server
     */
    public int getID() {
        return ID;
    }

    /**
     * This method is used to run the server thread.
     */
    public void run() {
        System.out.println("Server Thread " + ID + " running.");
        while (running) {
            try {
                server.handle(ID, streamIn.readUTF());
            } catch (IOException ioe) {
                System.out.println(ID + " ERROR reading: " + ioe.getMessage());
                server.remove(ID);
                connClose();
                break;
            }
        }
    }

    /**
     * This method is used to open the input and output streams for the server.
     * @throws IOException - the exception to throw
     */
    public void open() throws IOException {
        streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        streamOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    /**
     * This method is used to close the connection for the server.
     */
    public void connClose() {
        running = false;
        try {
            if (socket != null) socket.close();
            if (streamIn != null) streamIn.close();
            if (streamOut != null) streamOut.close();
        } catch (IOException ioe) {
            System.out.println("Error closing streams: " + ioe);
        }
    }
}