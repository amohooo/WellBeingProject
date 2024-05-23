package com.cab302.wellbeing.model;//Source:
//  Creating a simple Chat Client/Server Solution 
//  http://pirate.shu.edu/~wachsmut/Teaching/CSAS2214/Virtual/Lectures/chat-client-server.html


import java.io.*;
import java.net.Socket;

public class ChatServerThread extends Thread {
    private ChatServer server;
    private Socket socket;
    private int ID = -1;
    private DataInputStream streamIn;
    private DataOutputStream streamOut;
    private volatile boolean running = true;

    public ChatServerThread(ChatServer _server, Socket _socket) {
        super();
        server = _server;
        socket = _socket;
        ID = socket.getPort();
    }

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

    public int getID() {
        return ID;
    }

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

    public void open() throws IOException {
        streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        streamOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

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