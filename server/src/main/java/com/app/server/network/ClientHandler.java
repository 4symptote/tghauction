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
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(clientSocket.getInputStream());

            while (!clientSocket.isClosed()) {
                Request request = (Request) in.readObject();

                // Guard clause: ignore nulls
                if (request == null) {
                    continue;
                }

                Response response = handleRequest(request);

                out.writeObject(response);
                out.flush();
            }
        } catch (Exception e) {
            // Broadened catch: This will now catch structural/NPE issues before breaking the loop blindly
            System.out.println("Client disconnected or error occurred: " + e.getMessage());
        } finally {
            closeConnections();
        }
    }

    private Response handleRequest(Request request) {
        // As long as request is not null, this remains safe
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