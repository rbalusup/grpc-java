package com.rbalusup.grpc.calculator.client;

import com.proto.calculate.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {

    public static void main(String[] args) {
        CalculatorClient calculatorClient = new CalculatorClient();
        calculatorClient.run();
    }

    private void run() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        doSum(channel); // Unaru Client Call
        doPrimeNumberDecompose(channel); // Server Streamimg Client Call
        doComputeAverage(channel); // Client Streaming Client Call
        doFindMaximum(channel); // BiDi Streaming Client Call
        doErrorCallSquareRoot(channel); // Unary - Error Handling

        System.out.println("Shutting Down Channel");
        channel.shutdown();
    }

    private void doSum(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);

        SumRequest sumRequest = SumRequest.newBuilder()
                .setNumber1(3)
                .setNumber2(4)
                .build();

        SumResponse sumResponse = calculatorClient.sum(sumRequest);
        System.out.println(sumResponse.getSumNumber());
    }

    private void doPrimeNumberDecompose(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);

        PrimeNumberDecomposeRequest primeNumberDecomposeRequest = PrimeNumberDecomposeRequest.newBuilder()
                .setRandomNumber(120)
                .build();

        calculatorClient.primeNumberDecompose(primeNumberDecomposeRequest)
                .forEachRemaining(primeNumberDecomposeResponse -> {
                    System.out.println(primeNumberDecomposeResponse.getDecomposedNumber());
                });
    }

    private void doComputeAverage(ManagedChannel channel) {

        CalculatorServiceGrpc.CalculatorServiceStub calculatorAsyncClient = CalculatorServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<ComputeAverageRequest> requestStreamObserver = calculatorAsyncClient.computeAverage(new StreamObserver<ComputeAverageResponse>() {

            @Override
            public void onNext(ComputeAverageResponse value) {
                // we get response from the server
                System.out.println("Received a response from the server");
                System.out.println(value.getComputeAverage());

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

        // we send 10000 messages to our server
        for (int i = 0; i < 10000; i++) {
            requestStreamObserver.onNext(ComputeAverageRequest.newBuilder()
                .setComputeNumber(i)
                        .build());
        }

//        // StreamingMessage #1
//        System.out.println("Sending message #1");
//        requestStreamObserver.onNext(ComputeAverageRequest.newBuilder()
//                .setComputeNumber(1)
//                        .build());
//
//        // StreamingMessage #2
//        System.out.println("Sending message #2");
//        requestStreamObserver.onNext(ComputeAverageRequest.newBuilder()
//                .setComputeNumber(2)
//                        .build());
//
//        // StreamingMessage #3
//        System.out.println("Sending message #3");
//        requestStreamObserver.onNext(ComputeAverageRequest.newBuilder()
//                .setComputeNumber(3)
//                        .build());
//
//        // StreamingMessage #4
//        System.out.println("Sending message #4");
//        requestStreamObserver.onNext(ComputeAverageRequest.newBuilder()
//                .setComputeNumber(4)
//                .build());
//
//        // we expect the average to be 10/4 = 2.5.

        requestStreamObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doFindMaximum(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceStub calculatorAsyncClient = CalculatorServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<FindMaximumRequest> requestStreamObserver = calculatorAsyncClient.findMaximum((new StreamObserver<FindMaximumResponse>() {

            @Override
            public void onNext(FindMaximumResponse value) {
                // we get response from the server
                System.out.println("Got new maximum from the server: " + value.getMaximum());

                //onNext will be called only once

            }

            @Override
            public void onError(Throwable t) {
                // we get error from the server
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                // server is done sending us the data
                System.out.println("Server has done sending us something");
                latch.countDown();
                // onCompleted will be called right after onNext
            }
        }));

        Arrays.asList(3, 5, 17, 9, 8, 30, 12).forEach(number -> {
            System.out.println("Sending number: " + number);
            requestStreamObserver.onNext(FindMaximumRequest.newBuilder()
                    .setInputNumber(number)
                    .build());

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        requestStreamObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doErrorCallSquareRoot(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);

        int number = -1;

        try {
            calculatorClient.squareRoot(SquareRootRequest.newBuilder()
                    .setRootNumber(number)
                    .build());
        } catch (StatusRuntimeException sre) {
            System.out.println("Got an exception for square root");
            sre.printStackTrace();
        }
    }
}
