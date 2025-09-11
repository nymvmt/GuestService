package com.example.guest.client;

import com.example.guest.dto.AppointmentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceClient {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${services.appointment.url}")
    private String appointmentServiceUrl;
    
    
    /**
     * 전체 약속 목록 조회
     */
    public List<AppointmentResponse> getAllAppointments() {
        log.info("AppointmentService에서 전체 약속 목록 조회 시작");
        
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(appointmentServiceUrl)
                    .defaultHeader("Appointment-Agent", "appointment-service/1.0")
                    .build();
            
            List<AppointmentResponse> appointments = webClient
                    .get()
                    .uri("/appointments")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<AppointmentResponse>>() {})
                    .block();
            
            log.info("AppointmentService에서 전체 약속 목록 조회 성공 - 건수: {}", 
                    appointments != null ? appointments.size() : 0);
            return appointments;
            
        } catch (WebClientResponseException e) {
            log.error("AppointmentService 전체 약속 목록 조회 실패 - HTTP Status: {}, Body: {}", 
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("약속 목록 조회에 실패했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("AppointmentService 전체 약속 목록 조회 실패", e);
            throw new RuntimeException("약속 목록 조회에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 약속 상세 조회
     */
    public AppointmentResponse getAppointmentById(String appointmentId) {
        log.info("AppointmentService에서 약속 상세 조회 시작 - appointmentId: {}", appointmentId);
        
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(appointmentServiceUrl)
                    .defaultHeader("Appointment-Agent", "appointment-service/1.0")
                    .build();
            
            AppointmentResponse appointment = webClient
                    .get()
                    .uri("/appointments/{appointmentId}", appointmentId)
                    .retrieve()
                    .bodyToMono(AppointmentResponse.class)
                    .block();
            
            if (appointment != null) {
                log.info("AppointmentService에서 약속 상세 조회 성공 - appointmentId: {}, title: {}", 
                        appointmentId, appointment.getTitle());
            } else {
                log.warn("AppointmentService에서 약속을 찾을 수 없음 - appointmentId: {}", appointmentId);
            }
            
            return appointment;
            
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                log.warn("AppointmentService에서 약속을 찾을 수 없음 - appointmentId: {}", appointmentId);
                return null;
            }
            log.error("AppointmentService 약속 상세 조회 실패 - HTTP Status: {}, Body: {}", 
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("약속 조회에 실패했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("AppointmentService 약속 상세 조회 실패 - appointmentId: {}", appointmentId, e);
            throw new RuntimeException("약속 조회에 실패했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 약속 존재 여부 확인 (Guest 등록 전 검증용)
     */
    public boolean existsAppointment(String appointmentId) {
        AppointmentResponse appointment = getAppointmentById(appointmentId);
        return appointment != null;
    }
    
    /**
     * 호스트 ID로 약속 목록 조회 (상태 변경 권한 확인용)
     */
    public List<AppointmentResponse> getAppointmentsByHostId(String hostId) {
        log.info("AppointmentService에서 호스트 약속 목록 조회 시작 - hostId: {}", hostId);
        
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(appointmentServiceUrl)
                    .defaultHeader("Appointment-Agent", "appointment-service/1.0")
                    .build();
            
            List<AppointmentResponse> appointments = webClient
                    .get()
                    .uri("/appointments/host/{hostId}", hostId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<AppointmentResponse>>() {})
                    .block();
            
            log.info("AppointmentService에서 호스트 약속 목록 조회 성공 - hostId: {}, 건수: {}", 
                    hostId, appointments != null ? appointments.size() : 0);
            return appointments != null ? appointments : List.of();
            
        } catch (WebClientResponseException e) {
            log.error("AppointmentService 호스트 약속 목록 조회 실패 - HTTP Status: {}, Body: {}", 
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("호스트 약속 목록 조회에 실패했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("AppointmentService 호스트 약속 목록 조회 실패 - hostId: {}", hostId, e);
            throw new RuntimeException("호스트 약속 목록 조회에 실패했습니다: " + e.getMessage());
        }
    }
}
