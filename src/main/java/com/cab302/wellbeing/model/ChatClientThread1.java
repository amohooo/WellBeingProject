package com.cab302.wellbeing.model;//Source:
//  Creating a simple Chat Client1/Server Solution 
//  http://pirate.shu.edu/~wachsmut/Teaching/CSAS2214/Virtual/Lectures/chat-client-server.html


import com.cab302.wellbeing.controller.DeveloperController;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * This class is a thread for the chat client.
 * It provides methods to open, close, and run the client.
 */
public class ChatClientThread1 extends Thread {
    private Socket socket; // The socket for the client
    private DeveloperController client1; // The client controller
    private DataInputStream streamIn; // The input stream for the client
    private volatile boolean running = true; // The running state of the client

    /**
     * This method is used to initialize the chat client thread.
     * @param _client1 - the client controller
     * @param _socket - the socket for the client
     */
    public ChatClientThread1(DeveloperController _client1, Socket _socket) {
        client1 = _client1;
        socket = _socket;
        open();
        start();
    }

    /**
     * This method is used to open the input stream for the client.
     */
    public void open() {
        try {
            streamIn = new DataInputStream(socket.getInputStream());
        } catch (IOException ioe) {
            System.out.println("Error getting input stream: " + ioe);
            client1.connClose();
        }
    }

    /**
     * This method is used to close the connection for the client.
     */
    public void connClose() {
        running = false;
        try {
            if (streamIn != null) streamIn.close();
        } catch (IOException ioe) {
            System.out.println("Error closing input stream: " + ioe);
        }
    }

    /**
     * This method is used to run the client thread.
     */
    public void run() {
        while (running) {
            try {
                client1.handle(streamIn.readUTF());
            } catch (IOException ioe) {
                System.out.println("Listening error: " + ioe.getMessage());
                client1.connClose();
                break;
            }
        }
    }
}