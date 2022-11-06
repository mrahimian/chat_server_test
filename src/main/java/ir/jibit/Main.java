package ir.jibit;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    static volatile Long duration = 0L;

    public static void main(String[] args) throws InterruptedException {


        ExecutorService ex = Executors.newWorkStealingPool(16);
        var startTs = System.currentTimeMillis();
        var iterations = 1002;

        var latch = new CountDownLatch(iterations);
//        Thread.sleep(2000);

        for (int i = 0; i < iterations; i++) {
            ex.execute(() -> {
                var before = System.currentTimeMillis();
                var url = "http://localhost:80/user/rt" + getRandomNumberUsingNextInt(0, 100_000);
                var body = String.format("{\"to\":%d,\"message\":\"Hello World\"}", getRandomNumberUsingNextInt(0, 100_000));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> response = null;
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    System.out.println(response+" "+response.body() +" took " + (System.currentTimeMillis()-before)+"ms");
                } catch (IOException | InterruptedException e) {
                    System.err.println(e.getMessage());
                    ;
                }finally {
                    latch.countDown();
                }
                var after = System.currentTimeMillis();

//                System.out.println(response.body());
            });
        }
        ex.shutdown();
        latch.await();
        System.out.println("Took "+ (System.currentTimeMillis()-startTs) + " ms");
    }

    public static int getRandomNumberUsingNextInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }
}
