package com.bigsonata.requests.common.json;

import com.dslplatform.json.CompiledJson;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestDslJson {

  @CompiledJson
  static class Point {
    public int x = 0;
    public int y = 0;

    public Point(int x, int y) {
      this.x = x;
      this.y = y;
    }
  }

  @CompiledJson
  static class Circle {
    public int radius = 0;
    public Point point;

    public Circle(Point point, int radius) {
      this.point = point;
      this.radius = radius;
    }
  }

  @Test
  public void testSerialize() throws Exception {
    DslJsonCodec codec = new DslJsonCodec(Point.class, Circle.class);

    Point center = new Point(3, 5);
    String output = codec.serialize(center);

    assertNotEquals(output.length(), 0);

    Circle circle = new Circle(center, 5);
    output = codec.serialize(circle);
    System.out.println(output);
  }

  @Test
  public void testSerializeWithMultiplePrototypes() throws Exception {
    DslJsonCodec codec = new DslJsonCodec(Point.class, Circle.class);

    Point point = new Point(3, 5);
    String output = codec.serialize(point);
    assertNotEquals(output.length(), 0);

    Circle circle = new Circle(point, 6);
    output = codec.serialize(circle);

    assertNotEquals(output.length(), 0);
  }

  @Test
  public void testDeserialize() throws Exception {
    DslJsonCodec codec = new DslJsonCodec(Point.class, Circle.class);

    Point center = new Point(3, 5);
    String output = codec.serialize(center);

    Point point = codec.deserialize(Point.class, output.getBytes());

    assertEquals(center.x, point.x);
    assertEquals(center.y, point.y);

    Circle circle = new Circle(center, 5);
    output = codec.serialize(circle);

    Circle clone = codec.deserialize(Circle.class, output.getBytes());
    assertEquals(clone.radius, 5);
  }
}
