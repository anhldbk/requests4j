package com.bigsonata.requests.common.json;

/**
 * An interface for working with Json
 *
 * @author: Andy Le (@anhldbk)
 * @date: 8/19/18
 * @time: 10:30
 */
public interface JsonCodec {
  <T> String serialize(T input) throws Exception;

  <T> T deserialize(Class<T> prototype, byte[] input) throws Exception;
}
