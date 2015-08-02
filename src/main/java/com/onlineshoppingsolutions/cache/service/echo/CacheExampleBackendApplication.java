package com.onlineshoppingsolutions.cache.service.echo;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class CacheExampleBackendApplication extends Application<Configuration> {
    public static void main(String[] args) throws Exception {
        new CacheExampleBackendApplication().run(args);
    }

    @Override
    public String getName() {
        return "Response Service";
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        // nothing to do yet
    }

    @Override
    public void run(Configuration configuration,
                    Environment environment) {
        final CacheExampleBackendResource resource = new CacheExampleBackendResource();
        environment.jersey().register(resource);
    }

}