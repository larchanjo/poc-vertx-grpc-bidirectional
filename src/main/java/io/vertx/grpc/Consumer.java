package io.vertx.grpc;

import io.grpc.ManagedChannel;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.grpc.ServiceGrpc.ServiceVertxStub;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Consumer extends AbstractVerticle {

  private static final AtomicInteger requestCouter = new AtomicInteger(1);

  @Override
  public void start() {

    ManagedChannel channel = VertxChannelBuilder
        .forAddress(vertx, "localhost", 8080)
        .usePlaintext(true)
        .build();

    ServiceVertxStub stub = ServiceGrpc.newVertxStub(channel);
    stub.startChannel(exchange -> exchange.handler(response -> {
      System.out.println("Client: " + response);
      exchange.write(Request.newBuilder()
          .setId(requestCouter.getAndIncrement())
          .setContent(UUID.randomUUID().toString())
          .build());
    }).endHandler(v -> {
      System.out.println("Ended channel");
    }));

  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new Consumer());
  }

}