package com.nisum.carpool.data.util;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = {"com.nisum.carpool.data.domain"})
@EnableCassandraRepositories(basePackages = {"com.nisum.carpool.data"})
@EnableTransactionManagement
public class PersistenceConfig {

}
