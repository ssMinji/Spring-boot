package com.encore.wikidemo.controller;

import com.encore.wikidemo.client.ElasticSearchClient;
import com.encore.wikidemo.client.WIkipediaApiClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
public class SearchController {
    @Autowired
    private ElasticSearchClient elasticSearchClient;
    @Autowired
    private com.encore.wikidemo.client.WIkipediaApiClient wikiClient;
    
    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("search")
    public String search(String query, Model model) throws IOException {
        if (StringUtils.isNoneEmpty(query)) {
            model.addAttribute("elastic", elasticSearchClient.search(query));
            model.addAttribute("wiki", wikiClient.search(query));
            model.addAttribute("query", query);
        }
        return "search";
    }
}
