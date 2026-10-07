package API;

import Model.Card;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class YgoApiClient {
    private static final String RANDOM_CARD_URL = "https://db.ygoprodeck.com/api/v7/randomcard.php";
    private static final int MAX_INTENTOS = 20;

    private final HttpClient client;

    public YgoApiClient() {
        client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public Card getRandomMonsterCard() throws IOException, InterruptedException {
        for (int intento = 0; intento < MAX_INTENTOS; intento++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RANDOM_CARD_URL))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Error en la respuesta de la API: " + response.statusCode());
            }

            try {
                JSONObject json = new JSONObject(response.body());
                JSONArray dataArray = json.getJSONArray("data");

                if (dataArray.length() > 0) {
                    JSONObject cardJson = dataArray.getJSONObject(0);
                    String type = cardJson.optString("type", "");

                    if (type.contains("Monster")) {
                        String name = cardJson.getString("name");
                        int atk = cardJson.optInt("atk", 0);
                        int def = cardJson.optInt("def", 0);
                        String imageUrl = cardJson.getJSONArray("card_images")
                                .getJSONObject(0)
                                .getString("image_url");
                        return new Card(name, atk, def, imageUrl);
                    }
                }
            } catch (JSONException e) {
                System.err.println("Error parseando JSON, reintentando: " + e.getMessage());
            }

            Thread.sleep(500);
        }
        throw new IOException("No se pudo obtener una carta Monster después de " + MAX_INTENTOS + " intentos");
    }
}