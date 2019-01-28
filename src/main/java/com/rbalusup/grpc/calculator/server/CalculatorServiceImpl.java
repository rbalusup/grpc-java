package com.rbalusup.grpc.calculator.server;

import com.proto.calculate.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {

    @Override
    public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {

        SumResponse response = SumResponse.newBuilder()
                .setSumNumber(request.getNumber1() + request.getNumber2())
                .build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }

    @Override
    public void primeNumberDecompose(PrimeNumberDecomposeRequest request, StreamObserver<PrimeNumberDecomposeResponse> responseObserver) {
        Long number = request.getRandomNumber();
        Integer divisor = 2;

        while (number > 1) {
            if (number % divisor == 0) {
                number = number/divisor ;
                responseObserver.onNext(PrimeNumberDecomposeResponse.newBuilder()
                        .setDecomposedNumber(divisor)
                        .build());
            } else {
                divisor = divisor + 1;
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<ComputeAverageRequest> computeAverage(StreamObserver<ComputeAverageResponse> responseObserver) {
//        return super.computeAverage(responseObserver);
        StreamObserver<ComputeAverageRequest> requestStreamObserver = new StreamObserver<ComputeAverageRequest>() {

            int sum = 0;
            int count = 0;

            @Override
            public void onNext(ComputeAverageRequest value) {
                // client sends a message

                // increment the sum
                sum += value.getComputeNumber();

                // increment the count
                count += 1;

            }

            @Override
            public void onError(Throwable t) {
                // client sends a error
            }

            @Override
            public void onCompleted() {
                // client is done
                //This is when we want to send a response using responseObserver

                //compute average
                double computeAverageResponse = (double ) sum / count;


                responseObserver.onNext(ComputeAverageResponse.newBuilder()
                        .setComputeAverage(computeAverageResponse)
                        .build()
                );
                responseObserver.onCompleted();
            }
        };

        return requestStreamObserver;
    }

    @Override
    public StreamObserver<FindMaximumRequest> findMaximum(StreamObserver<FindMaximumResponse> responseObserver) {
//        return super.findMaximum(responseObserver);

        return new StreamObserver<FindMaximumRequest>() {

            int currentMaximum = 0;

            @Override
            public void onNext(FindMaximumRequest value) {
                // client sends a message

                // increment the sum
                int currentNumber = value.getInputNumber();

                if (currentNumber > currentMaximum) {
                    currentMaximum = currentNumber;
                    responseObserver.onNext(FindMaximumResponse.newBuilder()
                            .setMaximum(currentMaximum)
                            .build());
                } else {
                    // nothing
                }
            }

            @Override
            public void onError(Throwable t) {
                // client sends a error
            }

            @Override
            public void onCompleted() {
                // client is done
                //This is when we want to send a response using responseObserver
                responseObserver.onNext(FindMaximumResponse.newBuilder()
                        .setMaximum(currentMaximum)
                        .build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void squareRoot(SquareRootRequest request, StreamObserver<SquareRootResponse> responseObserver) {
//        super.squareRoot(request, responseObserver);

        Integer numberRoot = request.getRootNumber();

        if (numberRoot >= 0) {
            double sqrt = Math.sqrt(request.getRootNumber());

            responseObserver.onNext(SquareRootResponse.newBuilder()
                    .setSqrtNumber(sqrt)
                    .build());
        } else {
            // we construct the exception
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                    .withDescription("The number being sent is not positive")
                            .augmentDescription("Number sent: " + numberRoot)
                    .asRuntimeException()
            );
        }

        responseObserver.onCompleted();
    }
}
