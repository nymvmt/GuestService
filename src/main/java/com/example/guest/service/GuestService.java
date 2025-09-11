package com.example.guest.service;

import com.example.guest.client.AppointmentServiceClient;
import com.example.guest.dto.AppointmentResponse;
import com.example.guest.dto.request.GuestRequest;
import com.example.guest.dto.response.GuestResponse;
import com.example.guest.entity.Guest;
import com.example.guest.repository.GuestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class GuestService {

    @Autowired
    private GuestRepository guestRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private AppointmentServiceClient appointmentServiceClient;
    
    @Value("${services.appointment.url:http://localhost:8081}")
    private String appointmentServiceUrl;

    /**
     * 약속 참가자 등록
     */
    public GuestResponse createGuest(String appointmentId, GuestRequest request) {
        // 1. 약속 정보 조회하여 호스트 확인
        try {
            var appointment = appointmentServiceClient.getAppointmentById(appointmentId);
            if (appointment != null && appointment.getHostId().equals(request.getUser_id())) {
                throw new RuntimeException("내가 호스트인 약속에는 참여할 수 없어요!");
            }
        } catch (Exception e) {
            log.error("약속 정보 조회 실패: {}", appointmentId, e);
            throw new RuntimeException("약속 정보를 확인할 수 없습니다.");
        }
        
        // 2. 중복 체크 추가
        if (guestRepository.existsByAppointmentIdAndUserId(appointmentId, request.getUser_id())) {
            throw new RuntimeException("이미 해당 약속에 참여하고 있습니다.");
        }
        
        String guestId = generateGuestId();
        
        Guest guest = Guest.builder()
                .guest_id(guestId)
                .appointment_id(appointmentId)
                .user_id(request.getUser_id())
                .guest_status(request.getGuest_status() != null ? request.getGuest_status() : "coming")
                .created_at(LocalDateTime.now())
                .updated_at(LocalDateTime.now())
                .build();
        
        Guest savedGuest = guestRepository.save(guest);
        return convertToResponse(savedGuest);
    }

    /**
     * 약속 참가자 목록 조회
     */
    @Transactional(readOnly = true)
    public List<GuestResponse> getGuests(String appointmentId) {
        List<Guest> guests = guestRepository.findByAppointmentId(appointmentId);
        return guests.stream()
                .map(this::convertToResponse)
                .toList();
    }
    
    /**
     * 약속 개별 참가자 조회
     */


    @Transactional(readOnly = true)
    public GuestResponse getGuest(String appointmentId, String guestId) {
        log.info("🔍 [GuestService] getGuest 시작 - appointmentId: {}, guestId: {}", appointmentId, guestId);
        
        // 1. Guest 존재 여부 확인
        Optional<Guest> guestOptional = guestRepository.findById(guestId);
        if (guestOptional.isEmpty()) {
            log.error("❌ [GuestService] Guest를 찾을 수 없음 - guestId: {}", guestId);
            throw new RuntimeException("참가자를 찾을 수 없습니다. Guest ID: " + guestId);
        }
        
        Guest guest = guestOptional.get();
        
        // 2. 해당 Guest가 지정된 약속에 속하는지 확인
        if (!appointmentId.equals(guest.getAppointment_id())) {
            log.error("❌ [GuestService] Guest가 지정된 약속에 속하지 않음 - appointmentId: {}, guestId: {}, actualAppointmentId: {}", 
                    appointmentId, guestId, guest.getAppointment_id());
            throw new RuntimeException("해당 참가자는 이 약속에 속하지 않습니다. Guest ID: " + guestId + 
                    ", Appointment ID: " + appointmentId);
        }
        
        log.info("✅ [GuestService] getGuest 완료 - appointmentId: {}, guestId: {}, userId: {}, status: {}", 
                appointmentId, guestId, guest.getUser_id(), guest.getGuest_status());
        
        return convertToResponse(guest);
    }

    /**
     * 참가자 상태 조회 (상태만 String으로 반환)
     */
    @Transactional(readOnly = true)
    public String getGuestStatus(String appointmentId, String guestId) {
        log.info("🔍 [GuestService] getGuestStatus 시작 - appointmentId: {}, guestId: {}", appointmentId, guestId);
        
        // 기존 getGuest 메서드를 재사용하여 전체 정보 조회
        GuestResponse guestResponse = getGuest(appointmentId, guestId);
        
        // guest_status만 추출하여 반환
        String status = guestResponse.getGuest_status();
        
        log.info("✅ [GuestService] getGuestStatus 완료 - appointmentId: {}, guestId: {}, status: {}", 
                appointmentId, guestId, status);
        
        return status;
    }

    /**
     * 참가자 상태 변경
     */
    public GuestResponse updateGuestStatus(String appointmentId, String guestId, GuestRequest request, String userId) {
        // 1. Guest 존재 여부 확인
        if (!guestRepository.existsByGuestId(guestId)) {
            throw new RuntimeException("참가자가 삭제되었거나 존재하지 않습니다. Guest ID: " + guestId);
        }
        
        // 2. 호스트 권한 검증
        if (!isHost(appointmentId, userId)) {
            throw new RuntimeException("호스트가 아닌 사용자는 상태를 변경할 수 없습니다.");
        }
        
        // 3. 알림 도착 여부 체크는 Notification Service에서 처리
        // Guest Service는 상태 변경 요청을 받으면 바로 처리
        
        // 3. 상태 업데이트
        int updatedRows = guestRepository.updateGuestStatus(guestId, request.getGuest_status(), LocalDateTime.now());
        
        if (updatedRows == 0) {
            throw new RuntimeException("상태 업데이트에 실패했습니다. Guest ID: " + guestId);
        }
        
        // 4. 업데이트된 데이터 조회하여 반환
        Guest updatedGuest = guestRepository.findById(guestId)
                .orElseThrow(() -> new RuntimeException("업데이트된 데이터를 찾을 수 없습니다. Guest ID: " + guestId));
        
        return convertToResponse(updatedGuest);
    }

    /**
     * 참가자 삭제 (참가 취소)
     */
    public boolean deleteGuest(String appointmentId, String guestId) {
        if (guestRepository.existsByGuestId(guestId)) {
            guestRepository.deleteById(guestId);
            return true;
        }
        return false;
    }

    /**
     * Guest ID 생성 (순차적)
     */
    private String generateGuestId() {
        // 현재 시간 기반으로 순차적 ID 생성
        long timestamp = System.currentTimeMillis();
        return "guest" + timestamp;
    }

    /**
     * Entity를 Response DTO로 변환
     */
    private GuestResponse convertToResponse(Guest guest) {
        return GuestResponse.builder()
                .guest_id(guest.getGuest_id())
                .appointment_id(guest.getAppointment_id())
                .user_id(guest.getUser_id())
                .guest_status(guest.getGuest_status())
                .created_at(guest.getCreated_at())
                .updated_at(guest.getUpdated_at())
                .build();
    }

    /**
     * Appointment Service에서 전체 약속 목록 조회
     */
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> getAllAppointments() {
        try {
            String url = appointmentServiceUrl + "/appointments";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return ResponseEntity.ok((Map<String, Object>) response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Appointment Service 연결 실패: " + e.getMessage()));
        }
    }

    /**
     * Appointment Service에서 특정 약속 상세 조회
     */
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> getAppointment(String appointmentId) {
        try {
            String url = appointmentServiceUrl + "/appointments/" + appointmentId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return ResponseEntity.ok((Map<String, Object>) response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Appointment Service 연결 실패: " + e.getMessage()));
        }
    }

    /**
     * Appointment Service에서 약속 정보 조회 (내부용)
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getAppointmentInfo(String appointmentId) {
        try {
            String url = appointmentServiceUrl + "/appointments/" + appointmentId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return (Map<String, Object>) response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Appointment Service 연결 실패: " + e.getMessage());
        }
    }

    /**
     * 호스트 권한 검증 - WebClient 사용으로 변경 (디버깅 로그 추가)
     */
    private boolean isHost(String appointmentId, String userId) {
        log.info("🔍 호스트 권한 검증 시작 - appointmentId: {}, userId: {}", appointmentId, userId);
        
        try {
            AppointmentResponse appointment = appointmentServiceClient.getAppointmentById(appointmentId);
            if (appointment == null) {
                log.error("❌ 약속을 찾을 수 없음 - appointmentId: {}", appointmentId);
                throw new RuntimeException("약속을 찾을 수 없습니다. Appointment ID: " + appointmentId);
            }
            
            // camelCase 필드명 사용 (AppointmentResponse의 hostId 필드)
            String hostId = appointment.getHostId();
            log.info("📋 Appointment 정보 조회 성공 - appointmentId: {}, hostId: {}, title: {}", 
                    appointmentId, hostId, appointment.getTitle());
            
            boolean isHostUser = userId.equals(hostId);
            log.info("🔐 권한 검증 결과 - 요청사용자: '{}', 호스트: '{}', 권한있음: {}", 
                    userId, hostId, isHostUser);
            
            if (!isHostUser) {
                log.warn("⚠️ 호스트 권한 없음 - 요청사용자 '{}' != 호스트 '{}'", userId, hostId);
            }
            
            return isHostUser;
        } catch (Exception e) {
            log.error("💥 호스트 권한 검증 실패 - appointmentId: {}, userId: {}, error: {}", 
                    appointmentId, userId, e.getMessage(), e);
            throw new RuntimeException("호스트 권한 검증 실패: " + e.getMessage());
        }
    }

}