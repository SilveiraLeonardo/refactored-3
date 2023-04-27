
package okhttp3.guide;

import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.Gson;

public class PostExample {
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    final OkHttpClient client = new OkHttpClient();

    String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    String bowlingJson(String player1, String player2) {
        JsonObject root = new JsonObject();
        root.addProperty("winCondition", "HIGH_SCORE");
        root.addProperty("name", "Bowling");
        root.addProperty("round", 4);
        root.addProperty("lastSaved", 1367702411696L);
        root.addProperty("dateStarted", 1367702378785L);

        JsonObject player1Object = new JsonObject();
        player1Object.addProperty("name", player1);
        JsonArray player1History = new JsonArray();
        player1History.add(10);
        player1History.add(8);
        player1History.add(6);
        player1History.add(7);
        player1History.add(8);
        player1Object.add("history", player1History);
        player1Object.addProperty("color", -13388315);
        player1Object.addProperty("total", 39);

        JsonObject player2Object = new JsonObject();
        player2Object.addProperty("name", player2);
        JsonArray player2History = new JsonArray();
        player2History.add(6);
        player2History.add(10);
        player2History.add(5);
        player2History.add(10);
        player2History.add(10);
        player2Object.add("history", player2History);
        player2Object.addProperty("color", -48060);
        player2Object.addProperty("total", 41);

        JsonArray players = new JsonArray();
        players.add(player1Object);
        players.add(player2Object);

        root.add("players", players);

        Gson gson = new Gson();
        return gson.toJson(root);
    }

    public static void main(String[] args) throws IOException {
        PostExample example = new PostExample();
        String json = example.bowlingJson("Jesse", "Jake");
        String response = example.post("http://www.roundsapp.com/post", json);
        System.out.println(response);
    }
}
