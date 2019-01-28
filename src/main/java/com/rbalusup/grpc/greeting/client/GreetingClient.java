package com.rbalusup.grpc.greeting.client;

import com.proto.greet.*;
import io.grpc.*;
//import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
//import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

//import javax.net.ssl.SSLException;
//import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
//import com.proto.dummy.DummyServiceGrpc;

public class GreetingClient {
    public static void main(String[] args) { //throws SSLException {
        System.out.println("Hello I'm a gRPC client");

        GreetingClient greetingClient = new GreetingClient();
        greetingClient.run();

        // For Sync
        //DummyServiceGrpc.DummyServiceBlockingStub syncClient = DummyServiceGrpc.newBlockingStub(channel);

        //For Async
        //DummyServiceGrpc.DummyServiceFutureStub asyncClient = DummyServiceGrpc.newFutureStub(channel);
    }

    private void run() { // throws SSLException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
        System.out.println("Creating stub");

        /**
         * // With server authentication SSL/TLS
         * ManagedChannel channel = ManagedChannelBuilder.forAddress("myservice.example.com", 443)
         *     .build();
         * GreeterGrpc.GreeterStub stub = GreeterGrpc.newStub(channel);
         *
         * // With server authentication SSL/TLS; custom CA root certificates; not on Android
         * ManagedChannel channel = NettyChannelBuilder.forAddress("myservice.example.com", 443)
         *     .sslContext(GrpcSslContexts.forClient().trustManager(new File("roots.pem")).build())
         *     .build();
         * GreeterGrpc.GreeterStub stub = GreeterGrpc.newStub(channel);
         *
         */
        /**
         * Uncomment the below code, imports and throws SSLException sections, whenever you are using TLS/SSL
         */

//        ManagedChannel securedChannel = NettyChannelBuilder.forAddress("localhost", 50051)
//                .sslContext(GrpcSslContexts.forClient().trustManager(new File("ssl/ca.crt")).build())
//                .build();

//        doUnaryCall(channel);
//        doServerStreamingCall(channel);
//        doClientStreamingCall(channel);
//        doBiDirectionalStreamingCall(channel);
        doUnaryCallWithDeadline(channel);

        System.out.println("Shutting Down Channel");
        channel.shutdown();
    }

    private void doUnaryCall(ManagedChannel channel) {
        // Unary
        // created a greet service client (blocking - synchronous)
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        // created a protocol buffer greeting message
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Raj")
                .setLastName("Balusupati")
                .build();

        // do the same for a GreetRequest
        GreetRequest greetRequest = GreetRequest.newBuilder()
                .setOneTimeGreet(greeting)
                .build();

        // call the RPC and get back a GreetResponse (protocol buffers)
        GreetResponse greetResponse = greetClient.greet(greetRequest);
        System.out.println(greetResponse.getOneTimeResult());
    }

    private void doServerStreamingCall(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        // Server Streaming
        // we prepare the request
        GreetManyTimesRequest greetManyTimesRequest = GreetManyTimesRequest.newBuilder()
                .setManyTimeGreet(Greeting.newBuilder().setFirstName("Raj"))
                .build();

        greetClient.greetManyTimes(greetManyTimesRequest)
                .forEachRemaining(greetManyTimesResponse -> {
                    System.out.println(greetManyTimesResponse.getManyTimeGreetResult());
                });
    }

    private void doClientStreamingCall(ManagedChannel channel) {

        GreetServiceGrpc.GreetServiceStub greetAsyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<LongGreetRequest> requestStreamObserver = greetAsyncClient.longGreet(new StreamObserver<LongGreetResponse>() {

            @Override
            public void onNext(LongGreetResponse value) {
                // we get response from the server
                System.out.println("Received a response from the server: " + value.getLongGreetResult());

                //onNext will be called only once

            }

            @Override
            public void onError(Throwable t) {
                // we get error from the server
            }

            @Override
            public void onCompleted() {
                // server is done sending us the data
                System.out.println("Server has completed sending us something");
                latch.countDown();
                // onCompleted will be called right after onNext

            }
        });

        // StreamingMessage #1
        System.out.println("Sending message #1");
        requestStreamObserver.onNext(LongGreetRequest.newBuilder()
                .setLongGreet(Greeting.newBuilder()
                        .setFirstName("Raj")
                        .build())
                .build());

        // StreamingMessage #2
        System.out.println("Sending message #2");
        requestStreamObserver.onNext(LongGreetRequest.newBuilder()
                .setLongGreet(Greeting.newBuilder()
                        .setFirstName("Sekhar")
                        .build())
                .build());

        // StreamingMessage #3
        System.out.println("Sending message #3");
        requestStreamObserver.onNext(LongGreetRequest.newBuilder()
                .setLongGreet(Greeting.newBuilder()
                        .setFirstName("Varni")
                        .build())
                .build());

        requestStreamObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doBiDirectionalStreamingCall(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceStub greetAsyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<GreetEveryoneRequest> greetEveryoneRequestStreamObserver = greetAsyncClient.greetEveryone(new StreamObserver<GreetEveryoneResponse>() {

            @Override
            public void onNext(GreetEveryoneResponse value) {
                // we get response from the server
                System.out.println("Received a response from the server: " + value.getEveryOneGreetResult());
                //onNext will be called only once
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                // server is done sending us the data
                System.out.println("Server has done sending us something");
                latch.countDown();
                // onCompleted will be called right after onNext
            }
        });

        Arrays.asList("Raj", "Sekhar", "Rajath", "Karthik", "Varni").forEach(name -> {
            System.out.println("Sending: " + name);
            greetEveryoneRequestStreamObserver.onNext(GreetEveryoneRequest.newBuilder()
                .setEveryOneGreet(Greeting.newBuilder()
                        .setFirstName(name)
                        .build())
                .build());
        });

        greetEveryoneRequestStreamObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doUnaryCallWithDeadline(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        // first call with 3000ms deadline
        try {
            System.out.println("Sending a request with a deadline of 3000 ms");
            GreetWithDeadLineResponse greetWithDeadLineResponse = greetClient.withDeadline(Deadline.after(3000, TimeUnit.MILLISECONDS)).greetWithDeadLine(
                    GreetWithDeadLineRequest.newBuilder()
                            .setDlGreet(Greeting.newBuilder().setFirstName("Raj"))
                            .build());
            System.out.println(greetWithDeadLineResponse.getDlGreetResult());
        } catch (StatusRuntimeException sre) {
            if (sre.getStatus() == Status.DEADLINE_EXCEEDED) {
                System.out.println("Deadline has been exceeded, we don't want the response");
            } else {
                sre.printStackTrace();
            }
        }

        // second call with 100 ms deadline
        try {
            System.out.println("Sending a request with a deadline of 100 ms");
            GreetWithDeadLineResponse greetWithDeadLineResponse = greetClient.withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS)).greetWithDeadLine(
                    GreetWithDeadLineRequest.newBuilder()
                            .setDlGreet(Greeting.newBuilder().setFirstName("Raj"))
                            .build());
            System.out.println(greetWithDeadLineResponse.getDlGreetResult());
        } catch (StatusRuntimeException sre) {
            if (sre.getStatus() == Status.DEADLINE_EXCEEDED) {
                System.out.println("Deadline has been exceeded, we don't want the response");
            } else {
                sre.printStackTrace();
            }
        }
    }
}
