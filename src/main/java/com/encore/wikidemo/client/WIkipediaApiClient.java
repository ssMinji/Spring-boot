package com.encore.wikidemo.client;

import com.encore.wikidemo.model.SearchResult;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.ArrayList;
import java.util.List;

@Service
public class WIkipediaApiClient {
    private WebTarget root = ClientBuilder.newClient().target("https://ko.wikipedia.org/");
    private Gson gson = new Gson();

    public List<SearchResult> search(String query) {
        String body = root.path("/w/api.php")
                .queryParam("action", "opensearch")
                .queryParam("redirects", "resolve")
                .queryParam("search", query)
                .request().get().readEntity(String.class);
        JsonArray arr = gson.fromJson(body, JsonArray.class);
        JsonArray titles = arr.get(1).getAsJsonArray();
        JsonArray descs = arr.get(2).getAsJsonArray();
        List<SearchResult> results = new ArrayList<>();
        for (int i = 0; i < titles.size(); i++) {
            SearchResult searchResult = new SearchResult();
            searchResult.setTitle(titles.get(i).getAsString());
            searchResult.setDescription(descs.get(i).getAsString());
            results.add(searchResult);
        }
        return results;
    }
}
