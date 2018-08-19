package com.bigsonata.requests;

import org.asynchttpclient.Response;
import com.bigsonata.requests.common.Latency;

/**
 * Class represents Http responses
 *
 * @author: Andy Le (@anhldbk)
 * @date: 8/19/18
 * @time: 08:12
 */
public class HttpResponse extends Http {
  private Response response;
  public boolean isSuccess;
  public Exception reason; // if not success
  public byte[] body;
  public Latency latency;
  public HttpRequest request;
  protected final Requests requests;

  private HttpResponse(HttpRequest request) {
    this.request = request;
    this.requests = request.requests;
  }

  public HttpResponse(HttpRequest request, Response response) {
    this(request);
    this.isSuccess = true;
    this.body = response.getResponseBodyAsBytes();
    this.response = response;
  }

  public HttpResponse(HttpRequest request, Exception reason) {
    this(request);
    this.isSuccess = false;
    this.reason = reason;
  }

  public String header(String key) throws Exception {
    if (!isSuccess) {
      throw new Exception("Invalid response");
    }

    return response.getHeader(key);
  }

  public int statusCode() throws Exception {
    if (!isSuccess) {
      throw new Exception("Invalid response");
    }
    return response.getStatusCode();
  }

  public String headerContentType() throws Exception {
    return header(HEADER_CONTENT_TYPE);
  }

  public byte[] asBytes() {
    return body;
  }

  public String asString() {
    return new String(body);
  }

  public <T> T asObject(Class<T> prototype) throws Exception {
    return requests.toObject(prototype, body);
  }
}
