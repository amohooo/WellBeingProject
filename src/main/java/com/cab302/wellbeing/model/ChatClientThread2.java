package com.cab302.wellbeing.model;//Source:
//  Creating a simple Chat Client/Server Solution 
//  http://pirate.shu.edu/~wachsmut/Teaching/CSAS2214/Virtual/Lectures/chat-client-server.html


import com.cab302.wellbeing.controller.ContactController;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClientThread2 extends Thread {
    private Socket socket;
    private ContactController client2;
    private DataInputStream streamIn;
    private volatile boolean running = true;

    public ChatClientThread2(ContactController _client2, Socket _socket) {
        client2 = _client2;
        socket = _socket;
        open();
        start();
    }

    public void open() {
        try {
            streamIn = new DataInputStream(socket.getInputStream());
        } catch (IOException ioe) {
            System.out.println("Error getting input stream: " + ioe);
            client2.connClose();
        }
    }

    public void close() {
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
                client2.handle(streamIn.readUTF());
            } catch (IOException ioe) {
                System.out.println("Listening error: " + ioe.getMessage());
                client2.connClose();
                break;
            }
        }
    }
}