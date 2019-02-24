package com.encore.wikidemo.dao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class SimilarDAO {
    JdbcTemplate jdbc;

    public SimilarDAO() {
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl("jdbc:mysql://ls-0f21b16519d9a5d94fae17a404982290420e3981.ce70zohzx2qc.ap-northeast-2.rds.amazonaws.com:3306");
        ds.setUsername("dbmasteruser");
        ds.setPassword("encore365!");
        jdbc = new JdbcTemplate(ds);
    }

    public List<String> getSimilar(String title) {
        try {
            return Arrays.asList(jdbc.queryForObject("SELECT SIMILAR FROM SIMILARS WHERE TITLE=? LIMIT 1", String.class, title).split("\t"));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
