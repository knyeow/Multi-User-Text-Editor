package client;

import com.texteditor.proto.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GrpcClient {
    private final ManagedChannel channel;
    private final EditorServiceGrpc.EditorServiceStub asyncStub;
    private StreamObserver<ClientMessage> requestObserver;
    private final CountDownLatch done = new CountDownLatch(1);
    private final EditorGUI gui;
    private String username;

    public GrpcClient(String host, int port, EditorGUI gui) {
        System.out.println(">>>>> GrpcClient is being used right now! <<<<<");

        this.gui = gui;
        System.out.println("Connecting to server at " + host + ":" + port);
        
        // Ensure host is not null and trim any whitespace
        host = host != null ? host.trim() : "localhost";
        
        try {
            // Create channel with explicit TCP settings
            channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()  // Use plaintext (no TLS)
                .build();
                
            System.out.println("Channel created successfully");
            asyncStub = EditorServiceGrpc.newStub(channel);
        } catch (Exception e) {
            System.err.println("Failed to create channel: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create channel: " + e.getMessage(), e);
        }
    }

    public void start() {
        System.out.println("Starting gRPC stream");
        try {
            requestObserver = asyncStub.streamUpdates(new StreamObserver<ServerMessage>() {
                @Override
                public void onNext(ServerMessage message) {
                    System.out.println("Received message from server: " + message.getMessageTypeCase());
                    if (message.hasJoinResponse()) {
                        handleJoinResponse(message.getJoinResponse());
                    } else if (message.hasTextUpdate()) {
                        handleTextUpdate(message.getTextUpdate());
                    } else if (message.hasUserListUpdate()) {
                        handleUserListUpdate(message.getUserListUpdate());
                    } else if (message.hasErrorResponse()) {
                        handleErrorResponse(message.getErrorResponse());
                    }
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Error in gRPC stream: " + t.getMessage());
                    t.printStackTrace();
                    gui.showError("Connection error: " + t.getMessage());
                    done.countDown();
                }

                @Override
                public void onCompleted() {
                    System.out.println("gRPC stream completed");
                    done.countDown();
                }
            });
        } catch (Exception e) {
            System.err.println("Failed to start gRPC stream: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to start gRPC stream: " + e.getMessage(), e);
        }
    }

    private void handleJoinResponse(JoinResponse response) {
        System.out.println("Received join response: " + response.getSuccess());
        if (response.getSuccess()) {
            username = response.getMessage();
            gui.onJoinSuccess(username);
            gui.updateUserList(response.getCurrentUsersList());
        } else {
            gui.showError("Failed to join: " + response.getMessage());
        }
    }

    private void handleTextUpdate(TextUpdate update) {
        System.out.println("Received text update from: " + update.getUsername());
        gui.updateText(update.getContent());
    }

    private void handleUserListUpdate(UserListUpdate update) {
        System.out.println("Received user list update");
        gui.updateUserList(update.getUsersList());
    }

    private void handleErrorResponse(ErrorResponse error) {
        System.err.println("Received error from server: " + error.getErrorMessage());
        gui.showError(error.getErrorMessage());
    }

    public void sendTextUpdate(String text) {
        if (requestObserver != null) {
            ClientMessage message = ClientMessage.newBuilder()
                .setTextUpdate(TextUpdate.newBuilder()
                    .setUsername(username)
                    .setContent(text)
                    .build())
                .build();
            requestObserver.onNext(message);
        }
    }

    public void join(String username) {
        if (requestObserver != null) {
            ClientMessage message = ClientMessage.newBuilder()
                .setJoinRequest(JoinRequest.newBuilder()
                    .setUsername(username)
                    .build())
                .build();
            requestObserver.onNext(message);
        }
    }

    public void shutdown() {
        if (requestObserver != null) {
            requestObserver.onCompleted();
        }
        try {
            if (!channel.isShutdown()) {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
} 