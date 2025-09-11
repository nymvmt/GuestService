package com.example.guest.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

@Configuration
public class WebClientConfig {
    
    @Value("${app.ssl.trust-all:false}")
    private boolean trustAllCertificates;
    
    @Bean
    public WebClient.Builder webClientBuilder() throws SSLException {
        HttpClient httpClient;
        
        if (trustAllCertificates) {
            // 개발환경에서는 인증서 검증 비활성화
            SslContext sslContext = SslContextBuilder
                    .forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            
            httpClient = HttpClient.create()
                    .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
        } else {
            // Azure/프로덕션 환경에서는 기본 SSL 설정 사용
            httpClient = HttpClient.create();
        }
        
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}
