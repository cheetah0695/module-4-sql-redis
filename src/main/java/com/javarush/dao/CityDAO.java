package com.javarush.dao;

import com.javarush.domain.City;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

public class CityDAO {
    private final SessionFactory sessionFactory;

    public CityDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<City> findAll() {
        Query<City> query = sessionFactory.getCurrentSession()
                .createQuery("select c from City c", City.class);
        return query.getResultList();
    }

    public City findById(Integer id) {
        Query<City> query = sessionFactory.getCurrentSession()
                .createQuery("select c from City c where c.id = :id", City.class);
        query.setParameter("id", id);
        return query.getSingleResult();
    }

    public Integer getTotalCount() {
        Query<Integer> query = sessionFactory.getCurrentSession()
                .createQuery("select distinct count(c) from City c", Integer.class);
        return query.getSingleResult();
    }
}
