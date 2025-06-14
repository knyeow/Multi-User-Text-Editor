package com.texteditor.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Service definition for the text editor
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.60.0)",
    comments = "Source: editor.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class EditorServiceGrpc {

  private EditorServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "texteditor.EditorService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.texteditor.proto.ClientMessage,
      com.texteditor.proto.ServerMessage> getStreamUpdatesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "StreamUpdates",
      requestType = com.texteditor.proto.ClientMessage.class,
      responseType = com.texteditor.proto.ServerMessage.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<com.texteditor.proto.ClientMessage,
      com.texteditor.proto.ServerMessage> getStreamUpdatesMethod() {
    io.grpc.MethodDescriptor<com.texteditor.proto.ClientMessage, com.texteditor.proto.ServerMessage> getStreamUpdatesMethod;
    if ((getStreamUpdatesMethod = EditorServiceGrpc.getStreamUpdatesMethod) == null) {
      synchronized (EditorServiceGrpc.class) {
        if ((getStreamUpdatesMethod = EditorServiceGrpc.getStreamUpdatesMethod) == null) {
          EditorServiceGrpc.getStreamUpdatesMethod = getStreamUpdatesMethod =
              io.grpc.MethodDescriptor.<com.texteditor.proto.ClientMessage, com.texteditor.proto.ServerMessage>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "StreamUpdates"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.texteditor.proto.ClientMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.texteditor.proto.ServerMessage.getDefaultInstance()))
              .setSchemaDescriptor(new EditorServiceMethodDescriptorSupplier("StreamUpdates"))
              .build();
        }
      }
    }
    return getStreamUpdatesMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static EditorServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EditorServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EditorServiceStub>() {
        @java.lang.Override
        public EditorServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EditorServiceStub(channel, callOptions);
        }
      };
    return EditorServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static EditorServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EditorServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EditorServiceBlockingStub>() {
        @java.lang.Override
        public EditorServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EditorServiceBlockingStub(channel, callOptions);
        }
      };
    return EditorServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static EditorServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EditorServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EditorServiceFutureStub>() {
        @java.lang.Override
        public EditorServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EditorServiceFutureStub(channel, callOptions);
        }
      };
    return EditorServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Service definition for the text editor
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * Stream for real-time text updates and notifications
     * </pre>
     */
    default io.grpc.stub.StreamObserver<com.texteditor.proto.ClientMessage> streamUpdates(
        io.grpc.stub.StreamObserver<com.texteditor.proto.ServerMessage> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getStreamUpdatesMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service EditorService.
   * <pre>
   * Service definition for the text editor
   * </pre>
   */
  public static abstract class EditorServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return EditorServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service EditorService.
   * <pre>
   * Service definition for the text editor
   * </pre>
   */
  public static final class EditorServiceStub
      extends io.grpc.stub.AbstractAsyncStub<EditorServiceStub> {
    private EditorServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EditorServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EditorServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Stream for real-time text updates and notifications
     * </pre>
     */
    public io.grpc.stub.StreamObserver<com.texteditor.proto.ClientMessage> streamUpdates(
        io.grpc.stub.StreamObserver<com.texteditor.proto.ServerMessage> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getStreamUpdatesMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service EditorService.
   * <pre>
   * Service definition for the text editor
   * </pre>
   */
  public static final class EditorServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<EditorServiceBlockingStub> {
    private EditorServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EditorServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EditorServiceBlockingStub(channel, callOptions);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service EditorService.
   * <pre>
   * Service definition for the text editor
   * </pre>
   */
  public static final class EditorServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<EditorServiceFutureStub> {
    private EditorServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EditorServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EditorServiceFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_STREAM_UPDATES = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_STREAM_UPDATES:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.streamUpdates(
              (io.grpc.stub.StreamObserver<com.texteditor.proto.ServerMessage>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getStreamUpdatesMethod(),
          io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
            new MethodHandlers<
              com.texteditor.proto.ClientMessage,
              com.texteditor.proto.ServerMessage>(
                service, METHODID_STREAM_UPDATES)))
        .build();
  }

  private static abstract class EditorServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    EditorServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.texteditor.proto.Editor.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("EditorService");
    }
  }

  private static final class EditorServiceFileDescriptorSupplier
      extends EditorServiceBaseDescriptorSupplier {
    EditorServiceFileDescriptorSupplier() {}
  }

  private static final class EditorServiceMethodDescriptorSupplier
      extends EditorServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    EditorServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (EditorServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new EditorServiceFileDescriptorSupplier())
              .addMethod(getStreamUpdatesMethod())
              .build();
        }
      }
    }
    return result;
  }
}
