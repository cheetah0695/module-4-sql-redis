package com.javarush;

import com.javarush.dao.CityDAO;
import com.javarush.domain.City;
import com.javarush.redis.RedisUtil;
import com.javarush.util.SqlSessionFactoryBuilder;
import io.lettuce.core.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;

import java.util.List;

import static java.util.Objects.nonNull;

@Slf4j
public class App {
    private static SessionFactory sqlSessionFactory;
    private static RedisClient redisClient;

    static {
        sqlSessionFactory = SqlSessionFactoryBuilder.getSqlSessionFactory();
        redisClient = RedisUtil.prepareRedisClient();
    }

    public static void main(String[] args) {
        List<City> cities = CityDAO.getAllCities();
        RedisUtil.pushToRedis(cities);
        log.info("Loaded " + cities.size() + " cities");
        shutDown();
    }

    private static void shutDown() {
        if (nonNull(sqlSessionFactory)) {
            sqlSessionFactory.close();
        }
        if (nonNull(redisClient)) {
            redisClient.shutdown();
        }
    }
}
