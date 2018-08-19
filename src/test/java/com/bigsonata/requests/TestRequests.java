package com.bigsonata.requests;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TestRequests {
  static Requests requests;
  static final int THREADS = 8;
  static final int TIMEOUT = 5000;
  static final String URL = "https://bigsonata.com/";
  static final String USER_AGENT =
      "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";

  @BeforeClass
  public static void initialize() throws Exception {
    requests =
        Requests.newBuilder()
            .setTimeout(TIMEOUT)
            .setIoThreads(THREADS * 2)
            .build(AsyncRequests.class);
  }

  @AfterClass
  public static void dispose() {
    requests.dispose();
  }

  @Test
  public void testGET() throws Exception {
    Future<HttpResponse> responseFuture = requests.get(URL).headerUserAgent(USER_AGENT).exec();

    HttpResponse response = responseFuture.get();

    assertEquals(response.isSuccess, true);
    assertNotEquals(response.request, null);
  }

  @Test
  public void testLoad() throws InterruptedException {
    AtomicInteger error = new AtomicInteger(0);
    AtomicInteger success = new AtomicInteger(0);
    final int TIMES = 1000;
    final CountDownLatch latch = new CountDownLatch(TIMES);
    Runnable task =
        () -> {
          try {
            requests
                .get(URL)
                .headerUserAgent(USER_AGENT)
                .exec()
                .thenAccept(
                    response -> {
                      System.out.printf(
                          "request.id=%s\trequest.timestamp=%s\tresponse.latency=%s\n",
                          response.request.id, response.request.timeStamp, response.latency);
                      success.incrementAndGet();
                      latch.countDown();
                    })
                .join();

          } catch (Exception e) {
            e.printStackTrace();
            error.incrementAndGet();
          }
        };

    ExecutorService executorService = Executors.newFixedThreadPool(THREADS / 2);
    for (int i = 0; i < TIMES; i++) {
      executorService.submit(task);
    }

    latch.await();

    assertEquals(success.get(), TIMES);
    assertEquals(error.get(), 0);
  }
}
