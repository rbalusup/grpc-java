package com.rbalusup.grpc.greeting.server;

import com.proto.greet.*;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;

public class GreetServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {


    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
//        super.greet(request, responseObserver);
        // Extract the fields
        Greeting greeting = request.getOneTimeGreet();
        String firstName = greeting.getFirstName();

        // create the response
        String result = "Hello " + firstName;
        GreetResponse response = GreetResponse.newBuilder()
                .setOneTimeResult(result)
                .build();

        // send the response
        responseObserver.onNext(response);

        // complete the RPC call
        responseObserver.onCompleted();
    }

    @Override
    public void greetManyTimes(GreetManyTimesRequest request, StreamObserver<GreetManyTimesResponse> responseObserver) {
//        super.greetManyTimes(request, responseObserver);
        String firstName = request.getManyTimeGreet().getFirstName();

        try {
            for (int i = 0; i < 10; i++) {
                String result = "Hello " + firstName + ", response number: " + i;
                GreetManyTimesResponse response = GreetManyTimesResponse.newBuilder()
                        .setManyTimeGreetResult(result)
                        .build();
                responseObserver.onNext(response);
                Thread.sleep(1000L);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public StreamObserver<LongGreetRequest> longGreet(StreamObserver<LongGreetResponse> responseObserver) {
//        return super.longGreet(responseObserver);
        StreamObserver<LongGreetRequest> requestStreamObserver = new StreamObserver<LongGreetRequest>() {

            String longGreetResponse = "";

            @Override
            public void onNext(LongGreetRequest value) {
                // client sends a message
                longGreetResponse += "Hello " + value.getLongGreet().getFirstName() + "! ";
            }

            @Override
            public void onError(Throwable t) {
                // client sends a error
            }

            @Override
            public void onCompleted() {
                // client is done
                //This is when we want to send a response using responseObserver
                responseObserver.onNext(LongGreetResponse.newBuilder()
                        .setLongGreetResult(longGreetResponse)
                        .build()
                );
                responseObserver.onCompleted();
            }
        };

        return requestStreamObserver;
    }

    @Override
    public StreamObserver<GreetEveryoneRequest> greetEveryone(StreamObserver<GreetEveryoneResponse> responseObserver) {
//        return super.greetEveryone(responseObserver);
        StreamObserver<GreetEveryoneRequest> requestStreamObserver = new StreamObserver<GreetEveryoneRequest>() {

            @Override
            public void onNext(GreetEveryoneRequest value) {
                // client sends a message
                String result = "Hello " + value.getEveryOneGreet().getFirstName() + "! ";
                GreetEveryoneResponse greetEveryoneResponse =  GreetEveryoneResponse.newBuilder()
                        .setEveryOneGreetResult(result)
                        .build();
                responseObserver.onNext(greetEveryoneResponse);
            }

            @Override
            public void onError(Throwable t) {
                // client sends a error
            }

            @Override
            public void onCompleted() {
                // client is done
                //This is when we want to send a response using responseObserver
                responseObserver.onCompleted();
            }
        };

        return requestStreamObserver;
    }

    @Override
    public void greetWithDeadLine(GreetWithDeadLineRequest request, StreamObserver<GreetWithDeadLineResponse> responseObserver) {
//        super.greetWithDeadLine(request, responseObserver);

        Context current = Context.current();

        try {
            for (int i = 0; i < 3; i++) {
                if (!current.isCancelled()) {
                    System.out.println("sleep for 100 ms");
                    Thread.sleep(100);
                } else {
                    return;
                }
            }

            System.out.println("send response");
            responseObserver.onNext(GreetWithDeadLineResponse.newBuilder()
                    .setDlGreetResult("hello " + request.getDlGreet().getFirstName())
                    .build());

            // complete the RPC call
            responseObserver.onCompleted();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
