package com.encore.wikidemo.client;

import com.encore.wikidemo.model.SearchResult;
import com.google.gson.Gson;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ElasticSearchClient {
    private RestHighLevelClient client;
    private String index;
    private Gson gson = new Gson();

    public ElasticSearchClient(@Value("${elastic.host}") String host, @Value("${elastic.index.name}") String index) throws UnknownHostException {
        client = new RestHighLevelClient(RestClient.builder(new HttpHost(host, 9200)));
        this.index = index;
    }

    public List<SearchResult> search(String query) throws IOException {
        SearchRequest request = new SearchRequest();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.queryStringQuery(query));
        builder.size(10);
        request.source(builder);
        request.indices(index);

        SearchHits hits = client.search(request, RequestOptions.DEFAULT).getHits();
        List<SearchResult> result = new ArrayList<>();

        for (SearchHit hit : hits) {
            result.add(gson.fromJson(hit.getSourceAsString(), SearchResult.class));
        }

        return result;
    }

    @PreDestroy
    public void close() {
        try {
            client.close();
        } catch (Exception ignored) {
        }
    }
}
