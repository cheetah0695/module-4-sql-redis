package com.javarush.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.domain.City;
import com.javarush.domain.Country;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class RedisUtil {
    private static RedisClient redisClient;
    private static ObjectMapper objectMapper;

    static {
        redisClient = prepareRedisClient();
        objectMapper = new ObjectMapper();
    }

    public static RedisClient prepareRedisClient() {
        return RedisClient.create(RedisURI.create("localhost", 6379));
    }

    private static List<CityCountry> transformData(List<City> cities) {
        List<CityCountry> cityCountries = cities.stream()
                .map(city -> {
                    CityCountry cityCountry = new CityCountry();
                    cityCountry.setId(city.getId());
                    cityCountry.setName(city.getName());
                    cityCountry.setDistrict(city.getDistrict());
                    cityCountry.setPopulation(city.getPopulation());

                    Country country = city.getCountry();
                    cityCountry.setCountryCode(country.getCode());
                    cityCountry.setCountryName(country.getName());
                    cityCountry.setContinent(country.getContinent());
                    cityCountry.setAlternativeCountryCode(country.getAlternativeCountryCode());
                    cityCountry.setCountryRegion(country.getRegion());
                    cityCountry.setCountrySurfaceArea(country.getSurfaceArea());
                    cityCountry.setCountryPopulation(country.getPopulation());
                    Set<Language> languages = country.getLanguages().stream()
                            .map(countryLanguage -> {
                                Language language = new Language();
                                language.setLanguage(countryLanguage.getLanguage());
                                language.setOfficial(countryLanguage.getOfficial());
                                language.setPercentage(countryLanguage.getPercentage());
                                return language;
                            }).collect(Collectors.toSet());
                    cityCountry.setLanguages(languages);

                    return cityCountry;
                }).collect(Collectors.toList());

        return cityCountries;
    }

    public static void pushToRedis(List<City> cities) {
        List<CityCountry> dataForRedis = transformData(cities);
        try (StatefulRedisConnection<String, String> redisConnection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = redisConnection.sync();
            for (CityCountry cityCountry : dataForRedis) {
                sync.set(cityCountry.getId().toString(), objectMapper.writeValueAsString(cityCountry));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static List<CityCountry> readRedisDataWithIds(List<Integer> ids) {
        List<CityCountry> cities = new ArrayList<>();
        try (StatefulRedisConnection<String, String> redisConnection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = redisConnection.sync();
            for (Integer id : ids) {
                String json = sync.get(id.toString());
                if (json.isEmpty()) {
                    log.info("City with " + id + "not found in Redis!");
                } else {
                    cities.add(objectMapper.readValue(json, CityCountry.class));
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return cities;
    }
}