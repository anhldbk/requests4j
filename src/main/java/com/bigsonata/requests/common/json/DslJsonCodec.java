package com.bigsonata.requests.common.json;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.runtime.Settings;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A JsonCodec based on dsl-json
 *
 * @reference: https://github.com/ngs-doo/dsl-json
 * @author: Andy Le (@anhldbk)
 * @date: 8/19/18
 * @time: 9:40
 */
public class DslJsonCodec implements JsonCodec {
  private Map<String, DslJson> codec = new HashMap<>();

  /**
   * Constructor
   *
   * @param prototypes A list of Class(es) annotated with @CompiledJson
   */
  public DslJsonCodec(Class<?>... prototypes) {
    super();
    register(prototypes);
  }

  /**
   * Constructor
   *
   * @param prototypes A list of Class(es) annotated with @CompiledJson
   */
  public DslJsonCodec(Iterable<Class<?>> prototypes) {
    super();
    register(prototypes);
  }

  private DslJsonCodec register(Class<?>... prototypes) {
    for (Class<?> prototype : prototypes) {
      register(prototype);
    }
    return this;
  }

  private <T> DslJsonCodec register(Iterable<Class<?>> prototypes) {
    for (Class<?> prototype : prototypes) {
      register(prototype);
    }
    return this;
  }

  private <T> DslJsonCodec register(Class<T> prototype) {
    String className = prototype.getName();
    final DslJson dslJson =
        new DslJson<>(Settings.<T>withRuntime().allowArrayFormat(true).includeServiceLoader());
    codec.put(className, dslJson);
    return this;
  }

  @Override
  public <T> String serialize(T input) throws Exception {
    if (input == null) {
      throw new Exception("Invalid input");
    }
    String className = input.getClass().getName();
    if (!codec.containsKey(className)) {
      throw new Exception("Must invoke `register` first for class: " + className);
    }

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    codec.get(className).serialize(input, os);

    return os.toString();
  }

  @Override
  public <T> T deserialize(Class<T> prototype, byte[] input) throws Exception {
    if (input == null) {
      throw new Exception("Invalid input");
    }
    String className = prototype.getName();
    if (!codec.containsKey(className)) {
      throw new Exception("Must invoke `register` first for class: " + className);
    }

    return (T) codec.get(className).deserialize(prototype, input, input.length);
  }
}
