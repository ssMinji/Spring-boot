package com.encore.wikidemo.client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import com.encore.wikidemo.dao.SimilarDAO;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.index.query.functionscore.ScriptScoreFunctionBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.encore.wikidemo.model.SearchResult;
import com.google.gson.Gson;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.weightFactorFunction;

@Repository
public class ElasticSearchClient {
    private RestHighLevelClient client;
    private String index;
    private Gson gson = new Gson();
    @Autowired
    private SimilarDAO similarDAO;

    public ElasticSearchClient(@Value("${elastic.host}") String host, @Value("${elastic.index.name}") String index) throws UnknownHostException {
        client = new RestHighLevelClient(RestClient.builder(new HttpHost(host, 9200)));
        this.index = index;
    }

    public List<SearchResult> search(String query) throws IOException {
        SearchRequest request = new SearchRequest();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        String scoreCode = "_score+_score*((3*((Math.log(1+doc['score'].value-params.scmin))/(Math.log(params.scmax-params.scmin))))+(5*((Math.log(1+doc['pageview'].value-params.pvmin))/(Math.log(params.pvmax-params.pvmin))))+((doc['editcount'].value-params.editmin)/(params.editmax-params.editmin))+(Math.log(1+doc['descriptionsize'].value-params.desizemin)/Math.log(params.desizemax-params.desizemin)))";
        Map<String, Object> params = new HashMap<>();
        params.put("scmax", 10749.552d);
        params.put("scmin", 0);
        params.put("pvmax", 112242);
        params.put("pvmin", 0);
        params.put("editmax", 14413);
        params.put("editmin", 1);
        params.put("desizemax", 132042);
        params.put("desizemin", 0);

        Script script = new Script(ScriptType.INLINE, "painless", scoreCode, params);
        ScriptScoreFunctionBuilder scoreFunction = ScoreFunctionBuilders.scriptFunction(script);
        QueryBuilder q = QueryBuilders.boolQuery().must(QueryBuilders.queryStringQuery(query));

        FunctionScoreQueryBuilder.FilterFunctionBuilder title = new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                matchQuery("title", query),
                weightFactorFunction(2));
        FunctionScoreQueryBuilder.FilterFunctionBuilder general = new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                matchAllQuery(),
                scoreFunction.setWeight(15)
        );

        FunctionScoreQueryBuilder.FilterFunctionBuilder[] functions = {
                title, general
        };

        QueryBuilder functionScore = QueryBuilders.functionScoreQuery(q, functions).boostMode(CombineFunction.SUM);
        builder.query(functionScore);
        builder.size(10);
        System.out.println(builder.toString());
        request.source(builder);
        request.indices(index);

        SearchHits hits = client.search(request, RequestOptions.DEFAULT).getHits();
        List<SearchResult> result = new ArrayList<>();

        for (SearchHit hit : hits) {
            SearchResult searchResult = gson.fromJson(hit.getSourceAsString(), SearchResult.class);
            searchResult.setSimilars(similarDAO.getSimilar(searchResult.getTitle()));
            result.add(searchResult);
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
