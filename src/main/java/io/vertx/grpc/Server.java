package io.vertx.grpc;

import io.grpc.BindableService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.grpc.ServiceGrpc.ServiceVertxImplBase;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Server extends AbstractVerticle {

  private static final AtomicInteger responseCounter = new AtomicInteger(1);

  @Override
  public void start() {

    BindableService bindableService = new ServiceVertxImplBase() {

      @Override
      public void startChannel(GrpcBidiExchange<Request, Response> exchange) {
        // Start
        exchange.write(Response.newBuilder()
            .setId(responseCounter.getAndIncrement())
            .setContent(UUID.randomUUID().toString())
            .build());

        // Handle and keep conversation
        exchange.handler(request -> {
          try {
            TimeUnit.SECONDS.sleep(1);
            System.out.println("Server: " + request);
            exchange.write(Response.newBuilder()
                .setId(responseCounter.getAndIncrement())
                .setContent(UUID.randomUUID().toString())
                .build());
          }catch (Exception e) {
            // Do nothing
          }
        });
      }

    };

    VertxServer rpcServer = VertxServerBuilder
        .forPort(vertx, 8080)
        .addService(bindableService)
        .build();

    rpcServer.start(result -> {
      if (result.succeeded()) {
        System.out.println("Server started at 8080");
      } else {
        System.out.println("Fail to start server " + result.cause().getMessage());
      }
    });

  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new Server());
  }

}