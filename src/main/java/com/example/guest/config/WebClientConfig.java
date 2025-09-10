package com.example.guest.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

@Configuration
public class WebClientConfig {
    
    @Bean
    public WebClient.Builder webClientBuilder() throws SSLException {
        // SSL 설정 - 개발환경에서는 인증서 검증 비활성화
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        
        HttpClient httpClient = HttpClient.create()
                .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
        
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}
