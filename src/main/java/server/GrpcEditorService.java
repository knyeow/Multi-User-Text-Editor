package server;

import com.texteditor.proto.*;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GrpcEditorService extends EditorServiceGrpc.EditorServiceImplBase {
    private final ConcurrentHashMap<String, StreamObserver<ServerMessage>> clientStreams = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<String> connectedUsers = new CopyOnWriteArrayList<>();
    private String sharedText = "";

    @Override
    public StreamObserver<ClientMessage> streamUpdates(StreamObserver<ServerMessage> responseObserver) {
        return new StreamObserver<ClientMessage>() {
            private String currentUsername = null;

            @Override
            public void onNext(ClientMessage message) {
                if (message.hasJoinRequest()) {
                    handleJoinRequest(message.getJoinRequest(), responseObserver);
                } else if (message.hasTextUpdate()) {
                    handleTextUpdate(message.getTextUpdate());
                } else if (message.hasLeaveRequest()) {
                    handleLeaveRequest(message.getLeaveRequest());
                }
            }

            @Override
            public void onError(Throwable t) {
                if (currentUsername != null) {
                    handleLeaveRequest(LeaveRequest.newBuilder().setUsername(currentUsername).build());
                }
            }

            @Override
            public void onCompleted() {
                if (currentUsername != null) {
                    handleLeaveRequest(LeaveRequest.newBuilder().setUsername(currentUsername).build());
                }
                responseObserver.onCompleted();
            }

            private void handleJoinRequest(JoinRequest request, StreamObserver<ServerMessage> responseObserver) {
                String username = request.getUsername();
                if (connectedUsers.contains(username)) {
                    responseObserver.onNext(ServerMessage.newBuilder()
                        .setErrorResponse(ErrorResponse.newBuilder()
                            .setErrorMessage("Username already taken")
                            .build())
                        .build());
                    return;
                }

                currentUsername = username;
                connectedUsers.add(username);
                clientStreams.put(username, responseObserver);

                // Send join response
                responseObserver.onNext(ServerMessage.newBuilder()
                    .setJoinResponse(JoinResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Successfully joined")
                        .addAllCurrentUsers(connectedUsers)
                        .build())
                    .build());

                // Send current text
                responseObserver.onNext(ServerMessage.newBuilder()
                    .setTextUpdate(TextUpdate.newBuilder()
                        .setContent(sharedText)
                        .setCursorPosition(0)
                        .setUsername("")
                        .build())
                    .build());

                // Notify other clients
                broadcastUserList();
            }

            private void handleTextUpdate(TextUpdate update) {
                if (currentUsername == null) return;

                sharedText = update.getContent();
                broadcastTextUpdate(update);
            }

            private void handleLeaveRequest(LeaveRequest request) {
                String username = request.getUsername();
                if (username != null && connectedUsers.remove(username)) {
                    clientStreams.remove(username);
                    broadcastUserList();
                }
            }

            private void broadcastTextUpdate(TextUpdate update) {
                ServerMessage message = ServerMessage.newBuilder()
                    .setTextUpdate(update)
                    .build();

                for (StreamObserver<ServerMessage> observer : clientStreams.values()) {
                    observer.onNext(message);
                }
            }

            private void broadcastUserList() {
                ServerMessage message = ServerMessage.newBuilder()
                    .setUserListUpdate(UserListUpdate.newBuilder()
                        .addAllUsers(connectedUsers)
                        .build())
                    .build();

                for (StreamObserver<ServerMessage> observer : clientStreams.values()) {
                    observer.onNext(message);
                }
            }
        };
    }
} 