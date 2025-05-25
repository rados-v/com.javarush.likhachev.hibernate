package com.game.repository;

import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import java.util.Properties;

import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {

    private final SessionFactory sessionFactory;

    public PlayerRepositoryDB() {
        Properties properties = new Properties();
        properties.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQL10Dialect");
        properties.put(Environment.DRIVER, "org.postgresql.Driver");
        properties.put(Environment.URL, "jdbc:postgresql://localhost:5432/mydatabase");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "root");
        properties.put(Environment.SHOW_SQL, "true");
        properties.put(Environment.HBM2DDL_AUTO, "update");

//        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
//        properties.put(Environment.URL, "jdbc:p6spy:postgresql://localhost:5432/mydatabase");

        sessionFactory = new Configuration()
                .addAnnotatedClass(Player.class)
                .addProperties(properties)
                .buildSessionFactory();
    }



    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        try(Session session = sessionFactory.openSession()) {
            NativeQuery<Player> query = session.createNativeQuery("select * from rpg.player", Player.class);
            query.setFirstResult(pageNumber * pageSize);
            query.setMaxResults(pageSize);
            return query.list();
        }
    }

    @Override
    public int getAllCount() {
        try (Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createNamedQuery("player_getAllCount", Long.class);
            return query.uniqueResult().intValue();
        }
    }

    @Override
    public Player save(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(player);
            transaction.commit();
        }
        return null;
    }


    // Почему не void???  Если ты возвращаешь этот объект, то код, который вызывает этот метод, может сразу использовать обновлённые данные. Это может быть полезно, например, если обновление связано с генерацией новых значений, таких как временные метки, идентификаторы, версии данных и т. д.
    @Override
        public Player update(Player player) {
            try (Session session = sessionFactory.openSession()) {
                Transaction transaction = session.beginTransaction();
                try {
                    Player updatedPlayer = (Player) session.merge(player);
                    transaction.commit();
                    return updatedPlayer;
                } catch (Exception e) {
                    if (transaction != null) {
                        transaction.rollback();
                    }
                    e.printStackTrace();
                    return null;
                }
            }
        }

    @Override
    public Optional<Player> findById(long id) {
        try (Session session = sessionFactory.openSession()) {
            Player player = session.find(Player.class, id);
            return Optional.ofNullable(player);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }

    }

    @Override
    public void delete(Player player) {
        try(Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.remove(player);
            transaction.commit();
        }
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}