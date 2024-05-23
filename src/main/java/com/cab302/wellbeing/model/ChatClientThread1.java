package com.cab302.wellbeing.model;//Source:
//  Creating a simple Chat Client1/Server Solution 
//  http://pirate.shu.edu/~wachsmut/Teaching/CSAS2214/Virtual/Lectures/chat-client-server.html


import com.cab302.wellbeing.controller.DeveloperController;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClientThread1 extends Thread {
    private Socket socket;
    private DeveloperController client1;
    private DataInputStream streamIn;
    private volatile boolean running = true;

    public ChatClientThread1(DeveloperController _client1, Socket _socket) {
        client1 = _client1;
        socket = _socket;
        open();
        start();
    }

    public void open() {
        try {
            streamIn = new DataInputStream(socket.getInputStream());
        } catch (IOException ioe) {
            System.out.println("Error getting input stream: " + ioe);
            client1.connClose();
        }
    }

    public void connClose() {
        running = false;
        try {
            if (streamIn != null) streamIn.close();
        } catch (IOException ioe) {
            System.out.println("Error closing input stream: " + ioe);
        }
    }

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