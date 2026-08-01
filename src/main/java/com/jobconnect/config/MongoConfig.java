package com.jobconnect.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
//import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
//import de.flapdoodle.embed.mongo.distribution.Version;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

import java.util.concurrent.TimeUnit;


@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017/JobConnect}")
    private String mongoUri;

    @Override
    protected String getDatabaseName() {
        return "JobConnect";
    }

    @Override
    @Bean
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(mongoUri);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToSocketSettings(builder ->
                        builder.connectTimeout(30, TimeUnit.SECONDS)
                                .readTimeout(60, TimeUnit.SECONDS)
                )
                .applyToClusterSettings(builder ->
                        builder.serverSelectionTimeout(30, TimeUnit.SECONDS)
                )
                .applyToConnectionPoolSettings(builder ->
                        builder.maxConnectionIdleTime(60, TimeUnit.SECONDS)
                                .maxSize(50)
                                .minSize(5)
                )
                .build();

        return MongoClients.create(settings);
    }
//    @Bean
//    public IFeatureAwareVersion mongoVersion() {
//        return Version.Main.V4_4;
//    }
}

