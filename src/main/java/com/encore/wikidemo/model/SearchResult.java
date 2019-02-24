package com.encore.wikidemo.model;

import org.apache.commons.lang3.StringUtils;

public class SearchResult {
    private String title;
    private String description;
    private double score;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortenDescription() {
    	if(!getDescription().isEmpty()) {
    		return StringUtils.split(getDescription(), ".")[0];
    	}else {
    		return "No Description";
    	}
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
