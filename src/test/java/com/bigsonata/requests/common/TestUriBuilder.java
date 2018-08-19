package com.bigsonata.requests.common;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * TestURIBuilder
 *
 * @author: Andy Le (@anhldbk)
 * @date: 8/19/18
 * @time: 12:51
 */
public class TestUriBuilder {
  @Test
  public void testConstructUri() {
    UriBuilder builder =
        new UriBuilder()
            .setScheme("https")
            .setHost("bigsonata.com")
            .setPath("/java/examples/");

    assertEquals("https://bigsonata.com/java/examples/", builder.buildString());
  }

  @Test
  public void testConstructUriEncoded() {
    UriBuilder builder =
        new UriBuilder()
            .setScheme("https")
            .setHost("bigsonata.com")
            .addParameter("text", "data data")
            .setPath("/action_page2.php");

    assertEquals(
        "https://bigsonata.com/action_page2.php?text=data+data", builder.buildString());
  }

  @Test
  public void testConstructUriWithParameters() {
    List<UriBuilder.BasicNameValuePair> nameValuePairs =
        new ArrayList<UriBuilder.BasicNameValuePair>(1);
    nameValuePairs.add(new UriBuilder.BasicNameValuePair("test", "a"));
    nameValuePairs.add(new UriBuilder.BasicNameValuePair("test", "b"));
    UriBuilder builder =
        new UriBuilder()
            .setScheme("https")
            .setHost("bigsonata.com")
            .addParameter("test", "a")
            .addParameter("test", "b");

    assertEquals("https://bigsonata.com?test=a&test=b", builder.buildString());
  }
}
