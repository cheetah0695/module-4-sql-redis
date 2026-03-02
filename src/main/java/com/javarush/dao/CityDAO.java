package com.javarush.dao;

import com.javarush.domain.City;
import com.javarush.util.SqlSessionFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CityDAO {
    private static SessionFactory sessionFactory;

    static {
        sessionFactory = SqlSessionFactoryBuilder.getSqlSessionFactory();
    }

    private static List<City> findAll() {
        Query<City> query = sessionFactory.getCurrentSession()
                .createQuery("select c from City c", City.class);
        return query.getResultList();
    }

    private static City findById(Integer id) {
        Query<City> query = sessionFactory.getCurrentSession()
                .createQuery("select c from City c where c.id = :id", City.class);
        query.setParameter("id", id);
        return query.getSingleResult();
    }

    private static Integer getTotalCount() {
        Query<Integer> query = sessionFactory.getCurrentSession()
                .createQuery("select distinct count(c) from City c", Integer.class);
        return query.getSingleResult();
    }

    public static List<City> getAllCities() {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            List<City> cities = findAll();
            session.getTransaction().commit();
            return cities;
        }
    }

    public static List<City> getCitiesByIds(List<Integer> ids) {
        try (Session session = sessionFactory.getCurrentSession()) {
            List<City> cities = new ArrayList<>();
            session.beginTransaction();

            for (Integer id : ids) {
                City city = findById(id);
                if (city == null) {
                    log.info("City with " + id + "not found in MySql!");
                } else {
                    cities.add(findById(id));
                }
            }

            session.getTransaction().commit();
            return cities;
        }
    }
}
