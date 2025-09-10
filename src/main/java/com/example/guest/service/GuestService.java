package com.example.guest.service;

import com.example.guest.dto.request.GuestRequest;
import com.example.guest.dto.response.GuestResponse;
import com.example.guest.entity.Guest;
import com.example.guest.repository.GuestRepository;
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
public class GuestService {

    @Autowired
    private GuestRepository guestRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${appointment.service.url:http://localhost:8081}")
    private String appointmentServiceUrl;

    /**
     * 약속 참가자 등록
     */
    public GuestResponse createGuest(String appointmentId, GuestRequest request) {
        // 중복 체크 추가
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
     * 참가자 상태 조회
     */
    @Transactional(readOnly = true)
    public GuestResponse getGuestStatus(String appointmentId, String guestId) {
        Optional<Guest> guest = guestRepository.findById(guestId);
        if (guest.isEmpty()) {
            throw new RuntimeException("참가자가 삭제되었거나 존재하지 않습니다. Guest ID: " + guestId);
        }
        return convertToResponse(guest.get());
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
     * 호스트 권한 검증
     */
    private boolean isHost(String appointmentId, String userId) {
        try {
            Map<String, Object> appointment = getAppointmentInfo(appointmentId);
            String hostId = (String) appointment.get("host_id");
            return userId.equals(hostId);
        } catch (Exception e) {
            throw new RuntimeException("호스트 권한 검증 실패: " + e.getMessage());
        }
    }

}