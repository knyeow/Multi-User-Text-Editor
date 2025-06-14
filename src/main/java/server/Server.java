package server;

import io.grpc.ServerBuilder;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class Server {
    private static final int PORT = 5000;
    private io.grpc.Server server;

    public void start() throws IOException {
        System.out.println("Starting gRPC server...");
        
        // Create server with explicit TCP settings
        server = ServerBuilder.forPort(PORT)
            .addService(new GrpcEditorService())
            .build()
            .start();
        
        String serverIP = InetAddress.getLocalHost().getHostAddress();
        System.out.println("Server started successfully");
        System.out.println("Listening on port: " + PORT);
        System.out.println("Server IP address: " + serverIP);
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gRPC server...");
            Server.this.stop();
            System.out.println("Server shut down complete.");
        }));
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Initializing collaborative text editor server...");
        Server server = new Server();
        server.start();
        server.blockUntilShutdown();
    }
} 