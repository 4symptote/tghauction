package com.app.server.network;

import com.app.shared.network.BidRequest;
import com.app.shared.network.BidResponse;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

 //xử lý riêng cho từng client
public class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("[CLIENT HANDLER] Thread started for " + socket.getInetAddress());

        try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            Object input;
            while ((input = in.readObject()) != null) {
                if (input instanceof BidRequest) {
                    BidRequest request = (BidRequest) input;
                    System.out.println("[NETWORK] Processing request: " + request.getType());

                    BidResponse response = routeRequest(request);

                    out.writeObject(response);
                    out.flush();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[NETWORK] Connection closed for " + socket.getInetAddress());
        } finally {
            cleanup();
        }
    }

    //định tuyến yêu cầu
    private BidResponse routeRequest(BidRequest request) {
        switch (request.getType()) {
            case PLACE_BID:
                return new BidResponse(true, "Bid Request received and being processed by Network Layer");
            case LOGIN:
                return new BidResponse(true, "Login Successful (Network Mock)");
            default:
                return new BidResponse(false, "Unknown Request Type");
        }
    }

    private void cleanup() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
