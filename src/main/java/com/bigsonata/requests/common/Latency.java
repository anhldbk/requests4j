package com.bigsonata.requests.common;

/**
 * A class to represent latencies
 *
 * @author: Andy Le (@anhldbk)
 * @date: 8/19/18
 * @time: 12:10
 */
public class Latency {
  public long internal;
  public long network;

  public Latency(long internal, long network) {
    this.internal = internal;
    this.network = network;
  }

  @Override
  public String toString() {
    return "{internal: " + internal + ", network: " + network + "}";
  }
}
