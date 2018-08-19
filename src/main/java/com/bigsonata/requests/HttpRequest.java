package com.bigsonata.requests;

import com.bigsonata.requests.common.UriBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class represents Http requests
 *
 * @author: Andy Le (@anhldbk)
 * @date: 8/19/18
 * @time: 09:25
 */
public class HttpRequest extends Http {
  private static AtomicInteger COUNTER = new AtomicInteger(0);
  public final int id = COUNTER.incrementAndGet();

  protected final Requests requests;
  public String method = METHOD_GET;
  public String url;
  public byte[] body;
  public Map<String, String> headers = new HashMap<>();
  public long timeStamp = System.currentTimeMillis();

  protected HttpRequest(Requests requests, String method, String url) {
    this.requests = requests;
    this.method = method;
    this.headers.putAll(requests.builder.defaultHeaders);
    this.url = url;
  }

  protected HttpRequest(Requests requests, String method, UriBuilder uriBuilder) {
    this(requests, method, uriBuilder.buildString());
  }

  protected HttpRequest method(String method) {
    this.method = method;
    return this;
  }

  protected HttpRequest url(String url) {
    this.url = url;
    return this;
  }

  protected HttpRequest url(UriBuilder uriBuilder) {
    this.url = uriBuilder.buildString();
    return this;
  }

  public HttpRequest body(String body) {
    this.body = body.getBytes();
    return this;
  }

  public HttpRequest body(byte[] body) {
    this.body = body;
    return this;
  }

  public <T> HttpRequest bodyJson(T input) throws Exception {
    headerContentTypeJson();
    this.body = this.requests.toJson(input).getBytes();
    return this;
  }

  public HttpRequest headers(Map<String, String> headers) {
    this.headers = headers;
    return this;
  }

  public HttpRequest header(String key, String value) {
    this.headers.put(key, value);
    return this;
  }

  public HttpRequest headerContentType(String value) {
    return header(HEADER_CONTENT_TYPE, value);
  }

  public HttpRequest headerUserAgent(String userAgent) {
    return header(HEADER_USER_AGENT, userAgent);
  }

  public HttpRequest headerContentTypeJson() {
    return header(HEADER_CONTENT_TYPE, STRING_APPLICATION_JSON);
  }

  public HttpRequest headerAcceptJson() {
    return header(HEADER_ACCEPT, STRING_APPLICATION_JSON);
  }

  public HttpRequest basicAuth(String username, String password) {
    // TODO: implement this
    return this;
  }

  public CompletableFuture<HttpResponse> exec() {
    return this.requests.process(this);
  }

  public static class HttpDeleteRequest extends HttpRequest {
    public HttpDeleteRequest(Requests requests, String url) {
      super(requests, METHOD_DELETE, url);
    }
  }

  public static class HttpGetRequest extends HttpRequest {
    public HttpGetRequest(Requests requests, String url) {
      super(requests, METHOD_GET, url);
    }
  }

  public static class HttpPostRequest extends HttpRequest {
    public HttpPostRequest(Requests requests, String url) {
      super(requests, METHOD_POST, url);
    }
  }

  public static class HttpPutRequest extends HttpRequest {
    public HttpPutRequest(Requests requests, String url) {
      super(requests, METHOD_PUT, url);
    }
  }
}
