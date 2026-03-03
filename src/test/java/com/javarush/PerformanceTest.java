package com.javarush;

import com.javarush.dao.CityDAO;
import com.javarush.domain.City;
import com.javarush.redis.CityCountry;
import com.javarush.redis.RedisUtil;
import com.javarush.util.SqlSessionFactoryBuilder;
import io.lettuce.core.RedisClient;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PerformanceTest {
    private static SessionFactory sqlSessionFactory;
    private static RedisClient redisClient;
    private static List<Integer> cityIds = List.of(1, 20, 45, 100, 250, 300, 400, 500, 600, 700);
    private static Logger log;

    @BeforeAll
    static void setup() {
        sqlSessionFactory = SqlSessionFactoryBuilder.getSqlSessionFactory();
        redisClient = RedisUtil.prepareRedisClient();
        log = LoggerFactory.getLogger(App.class);
        RedisUtil.pushToRedis(CityDAO.getAllCities());
    }

    @AfterAll
    static void tearDown() {
        if (sqlSessionFactory != null && !sqlSessionFactory.isClosed()) {
            sqlSessionFactory.close();
        }
        if (redisClient != null) {
            redisClient.shutdown();
        }
    }

    @Test
    public void testMySqlPerformance() {
        long start = System.currentTimeMillis();
        List<City> cities = CityDAO.getCitiesByIds(cityIds);
        long duration = System.currentTimeMillis() - start;
        log.info("MySql reading " + cities.size() + " cities. Duration: " + duration + " ms");
    }

    @Test
    public void testRedisPerformance() {
        long start = System.currentTimeMillis();
        List<CityCountry> cities = RedisUtil.readRedisDataWithIds(cityIds);
        long duration = System.currentTimeMillis() - start;
        log.info("Redis reading " + cities.size() + " cities. Duration: " + duration + " ms");
    }
}
