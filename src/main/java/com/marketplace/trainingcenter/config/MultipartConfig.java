package com.marketplace.trainingcenter.config;


import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MultipartConfig {
    @Bean
    public TomcatServletWebServerFactory tomcatServletWebServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();

        factory.addConnectorCustomizers(connector -> {
            if (connector.getProtocolHandler() instanceof AbstractHttp11Protocol<?>) {
                // Increase the max size of POST requests
                ((AbstractHttp11Protocol<?>) connector.getProtocolHandler()).setMaxSavePostSize(100 * 1024 * 1024);
            }
        });

        return factory;
    }
}
