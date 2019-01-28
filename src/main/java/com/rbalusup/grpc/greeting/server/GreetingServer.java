package com.rbalusup.grpc.greeting.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

//import java.io.File;
import java.io.IOException;

public class GreetingServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello gRPC");

        //PlainText Server
        Server server = ServerBuilder.forPort(50051)
                // Enable TLS
                //.useTransportSecurity(certChainFile, privateKeyFile) // --> To enable TLS we need to uncomment this line
                .addService(new GreetServiceImpl())
                .build();


        /**
         * This is for Secure server. This is commented for time being
         * we can enable whenever we use TLS/SSL
         */
//        Server server = ServerBuilder.forPort(50051)
//                .useTransportSecurity(
//                        new File("ssl/server.crt"),
//                        new File("ssl/server.pem")
//                )
//                .build();

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            System.out.println("Received shutdown Request");
            server.shutdown();
            System.out.println("Successfully stopped the server");
        }));

        server.awaitTermination();
    }
}
