package com.bigsonata.requests;

import com.dslplatform.json.CompiledJson;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.bigsonata.requests.common.UriBuilder;
import com.bigsonata.requests.common.json.DslJsonCodec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TestJsonRequests {
  static Requests requests;
  static final int THREADS = 8;
  static final int TIMEOUT = 5000;
  static final String USER_NAME = "requests";
  static final String USER_AGENT =
      "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";

  @CompiledJson
  public static class User {
    public int id;
    public String first_name;
    public String last_name;
    public String avatar;

    @Override
    public boolean equals(Object that) {
      if (this == that) return true;
      if (!(that instanceof User)) return false;
      User user = (User) that;

      if (this.id != user.id) {
        return false;
      }
      if (!this.first_name.equals(user.first_name)) {
        return false;
      }
      if (!this.last_name.equals(user.last_name)) {
        return false;
      }
      if (!this.avatar.equals(user.avatar)) {
        return false;
      }
      return true;
    }
  }

  @CompiledJson
  static class UserData {
    public User data;
  }

  @BeforeClass
  public static void initialize() throws Exception {
    DslJsonCodec jsonCodec = new DslJsonCodec(User.class, UserData.class);

    requests =
        Requests.newBuilder()
            .setTimeout(TIMEOUT)
            .setIoThreads(THREADS)
            .setJsonCodec(jsonCodec)
            .build(AsyncRequests.class);
  }

  @AfterClass
  public static void dispose() {
    requests.dispose();
  }

  @Test
  public void testGET() throws Exception {
    int userId = 2;
    String API = "https://reqres.in/api/users/" + userId;
    UserData response =
        requests
            .get(API)
            .headerUserAgent(USER_AGENT)
            .headerAcceptJson()
            .exec()
            .get()
            .asObject(UserData.class);

    assertNotEquals(response, null);
    assertEquals(response.data.id, userId);
  }

  @Test
  public void testPOST() throws Exception {
    String API = "https://reqres.in/api/users";
    User user = new User();
    user.first_name = USER_NAME;

    HttpResponse response =
        requests.post(API).headerUserAgent(USER_AGENT).bodyJson(user).exec().get();

    User clone = response.asObject(User.class);
    assertNotEquals(clone, null);
    assertEquals(clone.first_name, USER_NAME);
  }

  @Test
  public void testPOSTWithUriBuilder() throws Exception {
    UriBuilder uriBuilder =
        UriBuilder.newInstance().setScheme("https").setHost("reqres.in").setPath("/api/users");
    User user = new User();
    user.first_name = USER_NAME;

    HttpResponse response =
        requests.post(uriBuilder).headerUserAgent(USER_AGENT).bodyJson(user).exec().get();

    User clone = response.asObject(User.class);
    assertNotEquals(clone, null);
    assertEquals(clone.first_name, USER_NAME);
  }
}
