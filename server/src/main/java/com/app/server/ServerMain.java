package com.app.server;

import com.app.server.network.ClientHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Khởi tạo ServerSocket và quản lý các kết nối Client bằng cách sử dụng Thread Pool
public class ServerMain {
    private static final int PORT = 8080;
    private static final int MAX_THREADS = 20;

    public static void main(String[] args) {
        // tạo threadpool
        ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERVER] Auction Server started on port " + PORT);
            System.out.println("[SERVER] Waiting for clients...");

            // While(true) vòng lặp để chấp nhận các kết nối socket new client
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[SERVER] New client connected from: " + clientSocket.getInetAddress());

                    //khởi tạo một tác vụ ClientHandler mới trong ThreadPol
                    threadPool.execute(new ClientHandler(clientSocket));
                } catch (IOException e) {
                    System.err.println("[SERVER] Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Fatal server error: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }
}
