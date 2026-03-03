package com.javarush.dao;

import com.javarush.domain.Country;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

public class CountryDAO {
    private final SessionFactory sessionFactory;

    public CountryDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<Country> findAll() {
        Query<Country> query = sessionFactory.getCurrentSession()
                .createQuery("select c from Country c " +
                        "left join fetch c.languages", Country.class);
        return query.getResultList();
    }

    public Country findById(Integer id) {
        Query<Country> query = sessionFactory.getCurrentSession()
                .createQuery("select c from Country c where c.id = :id", Country.class);
        query.setParameter("id", id);
        return query.getSingleResult();
    }

    public Integer getTotalCount() {
        Query<Integer> query = sessionFactory.getCurrentSession()
                .createQuery("select distinct count(c) from Country c", Integer.class);
        return query.getSingleResult();
    }
}
