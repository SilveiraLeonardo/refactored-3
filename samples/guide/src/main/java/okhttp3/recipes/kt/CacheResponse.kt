
package okhttp3.recipes;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class Authenticate {
  private final OkHttpClient client;

  public Authenticate() {
    client = new OkHttpClient.Builder()
        .authenticator((route, response) -> {
          if (response.request().header("Authorization") != null) {
            return null; // Give up, we've already attempted to authenticate.
          }

          System.out.println("Authenticating for response: " + response);
          System.out.println("Challenges: " + response.challenges());
          Properties credentials = loadCredentials();
          String username = credentials.getProperty("username");
          String password = credentials.getProperty("password");
          String credential = Credentials.basic(username, password);
          return response.request().newBuilder()
              .header("Authorization", credential)
              .build();
        })
        .build();
  }

  private Properties loadCredentials() {
    Properties properties = new Properties();
    try (FileInputStream in = new FileInputStream("credentials.properties")) {
      properties.load(in);
    } catch (IOException e) {
      System.err.println("Unable to read credentials file - using default credentials");
      properties.put("username", "defaultUsername");
      properties.put("password", "defaultPassword");
    }
    return properties;
  }

  public void run() throws Exception {
    Request request = new Request.Builder()
        .url("http://publicobject.com/secrets/hellosecret.txt")
        .build();

    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

      System.out.println(response.body().string());
    }
  }

  public static void main(String... args) throws Exception {
    new Authenticate().run();
  }
}
