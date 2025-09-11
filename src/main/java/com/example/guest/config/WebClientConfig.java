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
    
    @Value("${services.appointment.url}")
    private String appointmentServiceUrl;
    
    @Value("${services.appointment.api-key}")
    private String appointmentApiKey;
    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;
    
    @Bean
    public WebClient.Builder webClientBuilder() throws SSLException {
        HttpClient httpClient;
        
        // 환경별 SSL 설정
        if ("prod".equals(activeProfile)) {
            // 운영환경: 안전한 SSL 설정
            httpClient = HttpClient.create().secure();
        } else {
            // 개발환경: 인증서 검증 비활성화
            SslContext sslContext = SslContextBuilder
                    .forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            httpClient = HttpClient.create()
                    .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
        }
        
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
    
    @Bean("appointmentWebClient")
    public WebClient appointmentWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(appointmentServiceUrl)
                .defaultHeader("X-API-Key", appointmentApiKey)
                .defaultHeader("User-Agent", "guest-service/1.0")
                .build();
    }
}
