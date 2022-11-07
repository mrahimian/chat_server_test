package ir.jibit;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

public class Main {
    static volatile Long duration = 0L;

    public static void main(String[] args) throws Exception {


        Runtime.getRuntime().exec("ulimit -n 10000");
        ExecutorService ex = Executors.newWorkStealingPool(32);
        var startTs = System.currentTimeMillis();
        var iterations = 10001;

        var latch = new CountDownLatch(iterations-1);
//        Thread.sleep(2000);

//        HttpClient client = HttpClient.newHttpClient();
        HttpClient client = HttpClient.newBuilder().executor(ex).connectTimeout(Duration.ofMillis(10000)).build();//.newHttpClient();
        System.out.println(client.executor().get());
        IntStream.range(1, iterations).forEach(i -> {
            var url = "http://google.com";
            var body = String.format("{\"to\":%d,\"message\":\"Hello World\"}", getRandomNumberUsingNextInt(0, 100_000));

            HttpRequest request = HttpRequest.newBuilder()
                    .timeout(Duration.ofMillis(10000))
                    .uri(URI.create(url))
                    .GET()
                    .build();


            var before = System.currentTimeMillis();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept((res) -> {
                System.out.println(res + " " + res.body() + " took " + (System.currentTimeMillis() - before) + "ms");
                latch.countDown();
            }).exceptionally(e -> {
                System.out.println(e.getMessage());
                latch.countDown();
                return null;
            });

            var after = System.currentTimeMillis();

//                System.out.println(response.body());
        });
//                System.out.println(response.body());
        //ex.shutdown();
        latch.await();
        System.out.println("Took " + (System.currentTimeMillis() - startTs) + " ms");
    }

    public static int getRandomNumberUsingNextInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }
}
