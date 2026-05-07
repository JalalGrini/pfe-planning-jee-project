package com.pfe.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class HibernateUtil {
    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static final SessionFactory sessionFactory;

    static {
        try {
            String profile = System.getenv("DB_PROFILE");
            if (profile == null) {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream("config.properties")) {
                    props.load(fis);
                    profile = props.getProperty("db.profile", "dev");
                } catch (IOException e) {
                    logger.warn("config.properties introuvable, profil dev par défaut");
                    profile = "dev";
                }
            }

            Configuration cfg = new Configuration();
            if ("prod".equals(profile)) {
                cfg.configure("hibernate-prod.cfg.xml");
            } else {
                cfg.configure("hibernate.cfg.xml");
            }

            sessionFactory = cfg.buildSessionFactory();
            logger.info("SessionFactory initialisée pour le profil: {}", profile);
        } catch (Exception e) {
            logger.error("Échec d'initialisation d'Hibernate", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}