package com.pfe.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@PropertySource({"classpath:application.properties", "classpath:application-${spring.profiles.active:dev}.properties"})
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.hibernate.dialect}")
    private String dialect;

    @Value("${spring.hibernate.hbm2ddl.auto}")
    private String hbm2ddlAuto;

    @Value("${spring.hibernate.show_sql}")
    private String showSql;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(driverClassName);
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        return ds;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setPackagesToScan("com.pfe.model");
        
        Properties hibernateProperties = new Properties();
        hibernateProperties.put("hibernate.dialect", dialect);
        hibernateProperties.put("hibernate.hbm2ddl.auto", hbm2ddlAuto);
        hibernateProperties.put("hibernate.show_sql", showSql);
        sessionFactory.setHibernateProperties(hibernateProperties);
        
        return sessionFactory;
    }

    @Bean
    public org.springframework.orm.hibernate5.HibernateTransactionManager transactionManager() {
        org.springframework.orm.hibernate5.HibernateTransactionManager txManager = new org.springframework.orm.hibernate5.HibernateTransactionManager();
        txManager.setSessionFactory(sessionFactory().getObject());
        return txManager;
    }
}