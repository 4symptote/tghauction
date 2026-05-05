package com.app.server.network;

import com.app.shared.network.Request;
import com.app.shared.network.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            // CRITICAL: Always create ObjectOutputStream first and flush it before ObjectInputStream!
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(clientSocket.getInputStream());

            // Continuously listen for requests from this connected client
            while (!clientSocket.isClosed()) {
                // Wait for the client to send a request
                Request request = (Request) in.readObject();

                // Process the request
                Response response = handleRequest(request);

                // Send the response back to the client
                out.writeObject(response);
                out.flush(); // Don't forget to flush!
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client disconnected or error occurred: " + e.getMessage());
        } finally {
            closeConnections();
        }
    }

    private Response handleRequest(Request request) {
        switch (request.type()) {
            case LOGIN:
                String username = (String) request.payload();
                System.out.println("User logged in: " + username);
                return new Response(true, "Login successful", username);
            default:
                return new Response(false, "Unknown command", null);
        }
    }

    private void closeConnections() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}