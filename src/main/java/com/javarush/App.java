package com.javarush;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.dao.CityDAO;
import com.javarush.dao.CountryDAO;
import com.javarush.domain.City;
import com.javarush.domain.Country;
import com.javarush.redis.CityCountry;
import com.javarush.redis.Language;
import com.javarush.util.Util;
import io.lettuce.core.RedisClient;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

public class App {
    private final Util util;
    private final SessionFactory sqlSessionFactory;
    private final RedisClient redisClient;
    private final ObjectMapper objectMapper;
    private final CityDAO cityDAO;
    private final CountryDAO countryDAO;

    public static void main(String[] args) {
        App app = new App();
        List<City> cities = app.getCities();
        List<CityCountry> dataForRedis = app.transformData(cities);
        app.shutDown();
    }

    public App() {
        objectMapper = new ObjectMapper();
        this.util = new Util();
        sqlSessionFactory = util.getSqlSessionFactory();
        cityDAO = new CityDAO(sqlSessionFactory);
        countryDAO = new CountryDAO(sqlSessionFactory);

        redisClient = util.prepareRedisClient();
    }

    private List<City> getCities() {
        try (Session session = sqlSessionFactory.getCurrentSession()) {
            session.beginTransaction();
            List<City> cities = cityDAO.findAll();
            session.getTransaction().commit();
            return cities;
        }
    }

    private void shutDown() {
        if (nonNull(sqlSessionFactory)) {
            sqlSessionFactory.close();
        }
        if (nonNull(redisClient)) {
            redisClient.shutdown();
        }
    }

    private List<CityCountry> transformData(List<City> cities) {
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
}
