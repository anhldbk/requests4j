package com.bigsonata.requests;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import com.bigsonata.requests.common.json.JsonCodec;
import com.bigsonata.requests.common.UriBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Base class for crafting Requests instances
 *
 * @author: Andy Le (@anhldbk)
 * @date: 8/19/18
 * @time: 11:07
 */
public abstract class Requests {
  protected final Builder builder;

  protected Requests(Builder builder) throws Exception {
    this.builder = builder;
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
  public abstract <T> T toObject(Class<T> prototype, byte[] input) throws Exception;

  public <T> T toObject(Class<T> prototype, String input) throws Exception {
    return toObject(prototype, input.getBytes());
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
  public abstract <T> String toJson(T input) throws Exception;

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    protected boolean proxyEnabled = false;
    protected String proxyHost = "";
    protected int proxyPort = 80;
    protected int maxConnections = 16;
    protected int ioThreads = 8;
    protected int timeout = 1000; // ms
    protected JsonCodec jsonCodec = null;
    protected Map<String, String> defaultHeaders = new HashMap<>();

    public <T extends Requests> T build(Class<T> prototype) throws Exception {
      Class[] args = new Class[1]; // Our constructor has 1 arguments
      args[0] = Builder.class;
      return (T) prototype.getDeclaredConstructor(args).newInstance(this);
    }

    /**
     * [Optional] Configure proxy to use
     *
     * @param host Host
     * @param port Port
     * @return Current instance of Builder
     */
    public Builder setProxy(String host, int port) {
      this.proxyEnabled = true;
      this.proxyHost = host;
      this.proxyPort = port;
      return this;
    }

    /**
     * [Optional] Set maximum connections. Default is 16.
     *
     * @param maxConnections Max connections
     * @return Current instance of Builder
     */
    public Builder setMaxConnections(int maxConnections) {
      this.maxConnections = maxConnections;
      return this;
    }

    /**
     * [Optional] Set request timeout. Default is 1000ms
     *
     * @param timeout Timeout (in ms)
     * @return Current instance of Builder
     */
    public Builder setTimeout(int timeout) {
      this.timeout = timeout;
      return this;
    }

    /**
     * [Optional] Set JsonCodec for working with Json data
     *
     * @param jsonCodec A JsonCodec-derived instance
     * @return Current instance of Builder
     */
    public Builder setJsonCodec(JsonCodec jsonCodec) {
      this.jsonCodec = jsonCodec;
      return this;
    }

    /**
     * [Optional] Set the number of IO threads. Default is 8.
     *
     * @param ioThreads The number
     * @return Current instance of Builder
     */
    public Builder setIoThreads(int ioThreads) {
      this.ioThreads = ioThreads;
      return this;
    }

    /**
     * [Optional] Set default headers to send with every requests
     *
     * @param key Header key
     * @param value Header value
     * @return Current instance of Builder
     */
    public Builder setDefaultHeader(String key, String value) {
      defaultHeaders.put(key, value);
      return this;
    }
  }

  /** Dispose allocated resources */
  protected abstract void dispose();

  protected static void onException(Logger logger, Exception e) {
    logger.error("Failed to consume request. Reason: {}", e.getMessage());
    logger.error("Stacktrace: {}", ExceptionUtils.getStackTrace(e));
  }

  protected void initializeShutdownHook() {
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  dispose();
                }));
  }

  /**
   * Process an Http Request and return a future resolving into an Http Response
   *
   * @param httpRequest The request
   * @return The future
   */
  public abstract CompletableFuture<HttpResponse> process(HttpRequest httpRequest);

  public HttpRequest get(String url) {
    return new HttpRequest.HttpGetRequest(this, url);
  }

  public HttpRequest get(UriBuilder uriBuilder) {
    return new HttpRequest.HttpGetRequest(this, uriBuilder.buildString());
  }

  public HttpRequest post(String url) {
    return new HttpRequest.HttpPostRequest(this, url);
  }

  public HttpRequest post(UriBuilder uriBuilder) {
    return new HttpRequest.HttpPostRequest(this, uriBuilder.buildString());
  }

  public HttpRequest delete(String url) {
    return new HttpRequest.HttpDeleteRequest(this, url);
  }

  public HttpRequest delete(UriBuilder uriBuilder) {
    return new HttpRequest.HttpDeleteRequest(this, uriBuilder.buildString());
  }

  public HttpRequest put(String url) {
    return new HttpRequest.HttpPutRequest(this, url);
  }

  public HttpRequest put(UriBuilder uriBuilder) {
    return new HttpRequest.HttpPutRequest(this, uriBuilder.buildString());
  }
}
