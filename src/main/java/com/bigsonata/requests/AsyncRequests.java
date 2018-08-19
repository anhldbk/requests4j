package com.bigsonata.requests;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.asynchttpclient.*;
import org.asynchttpclient.proxy.ProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bigsonata.requests.common.Latency;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static org.asynchttpclient.Dsl.config;

/**
 * A Requests implementation based on asynchttpclient library
 *
 * @author: Andy Le (@anhldbk)
 * @date: 8/19/18
 * @time: 12:07
 */
public class AsyncRequests extends Requests {
  protected static final Logger LOGGER = LoggerFactory.getLogger(AsyncRequests.class);
  protected EventLoopGroup eventLoopGroup = null;
  protected AsyncHttpClient httpService;
  protected ExecutorService ioExecutorService;
  protected ProxyServer proxyServer = null;
  private Map<String, Function<String, BoundRequestBuilder>> requestFactories = new HashMap<>();

  public AsyncRequests(Builder builder) throws Exception {
    super(builder);
    initialize();
  }

  private void initRequestFactories() {
    requestFactories.put(HttpRequest.METHOD_GET, httpService::prepareGet);
    requestFactories.put(HttpRequest.METHOD_POST, httpService::preparePost);
    requestFactories.put(HttpRequest.METHOD_DELETE, httpService::prepareDelete);
    requestFactories.put(HttpRequest.METHOD_PUT, httpService::preparePut);
  }

  protected BoundRequestBuilder getRequestBuilder(HttpRequest httpRequest) {
    // We strictly control HttpRequest and their factories
    // So there's no need to check if there's any factory associated with an Http method
    BoundRequestBuilder requestBuilder =
        requestFactories.get(httpRequest.method).apply(httpRequest.url);
    requestBuilder.setBody(httpRequest.body);

    for (String header : httpRequest.headers.keySet()) {
      requestBuilder.addHeader(header, httpRequest.headers.get(header));
    }

    if (builder.proxyEnabled) {
      requestBuilder.setProxyServer(proxyServer);
    }
    return requestBuilder;
  }

  /**
   * Deserialize a byte array into an object instance NOTE: You have to register a JsonCodec with
   * `Requests.Builder` to use
   *
   * @param prototype A class
   * @param input Byte array
   * @param <T> A class
   * @return An object
   * @throws Exception Throws exception if there's something wrong
   */
  public <T> T toObject(Class<T> prototype, byte[] input) throws Exception {
    if (this.builder.jsonCodec == null) {
      throw new Exception("Now JsonCodec provided");
    }
    return this.builder.jsonCodec.deserialize(prototype, input);
  }

  /**
   * Serialize an object instance into a Json string NOTE: You have to register a JsonCodec with
   * `Requests.Builder` to use
   *
   * @param input The instance
   * @param <T> A class
   * @return Json string
   * @throws Exception Throws exception if there's something wrong
   */
  public <T> String toJson(T input) throws Exception {
    if (this.builder.jsonCodec == null) {
      throw new Exception("Now JsonCodec provided");
    }
    return this.builder.jsonCodec.serialize(input);
  }

  /** Dispose allocated resources */
  protected void dispose() {
    if (eventLoopGroup == null) {
      LOGGER.warn("Requests is NOT initialized");
      return;
    }

    eventLoopGroup.shutdownGracefully();
    try {
      httpService.close();
    } catch (IOException e) {
      onException(LOGGER, e);
    }
    ioExecutorService.shutdown();
  }

  protected void initialize() throws Exception {
    LOGGER.info("Initializing Requests...");
    LOGGER.info("> timeout={}", builder.timeout);
    LOGGER.info("> ioThreads={}", builder.ioThreads);
    LOGGER.info("> maxConnections={}", builder.maxConnections);

    eventLoopGroup = new NioEventLoopGroup(builder.ioThreads);
    DefaultAsyncHttpClientConfig config =
        config()
            .setMaxConnections(builder.maxConnections)
            .setMaxConnectionsPerHost(builder.maxConnections)
            .setRequestTimeout(builder.timeout)
            .setConnectTimeout(builder.timeout)
            .setEventLoopGroup(eventLoopGroup)
            .setIoThreadsCount(builder.ioThreads)
            .build();
    httpService = Dsl.asyncHttpClient(config);
    ioExecutorService = Executors.newFixedThreadPool(builder.ioThreads);

    initializeProxy();
    initRequestFactories();
    initializeShutdownHook();

    LOGGER.info("Requests is initialized");
  }

  protected void initializeProxy() {
    if (!builder.proxyEnabled) {
      LOGGER.info("No proxy configured");
      return;
    }
    LOGGER.info("Initializing Proxy...");
    LOGGER.info("> proxyHost={}", builder.proxyHost);
    LOGGER.info("> proxyPort={}", builder.proxyPort);
    proxyServer = new ProxyServer.Builder(builder.proxyHost, builder.proxyPort).build();
    LOGGER.info("Proxy is initialized");
  }

  public CompletableFuture<HttpResponse> process(HttpRequest httpRequest) {
    CompletableFuture<HttpResponse> result = new CompletableFuture<>();
    BoundRequestBuilder requestBuilder = getRequestBuilder(httpRequest);

    long currentTime = System.currentTimeMillis();
    long internalLatency = currentTime - httpRequest.timeStamp;

    final ListenableFuture<Response> futureResponse = requestBuilder.execute();

    Runnable responseHandler =
        () -> {
          HttpResponse httpResponse;
          try {
            Response response = futureResponse.get();
            httpResponse = new HttpResponse(httpRequest, response);
          } catch (Exception e) {
            httpResponse = new HttpResponse(httpRequest, e);
          }

          long networkLatency = System.currentTimeMillis() - currentTime;
          httpResponse.latency = new Latency(internalLatency, networkLatency);
          result.complete(httpResponse);
        };

    futureResponse.addListener(responseHandler, ioExecutorService);

    return result;
  }
}
