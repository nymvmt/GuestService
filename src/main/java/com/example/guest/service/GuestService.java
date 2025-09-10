package com.example.guest.service;

import com.example.guest.dto.request.GuestRequest;
import com.example.guest.dto.response.GuestResponse;
import com.example.guest.entity.Guest;
import com.example.guest.repository.GuestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class GuestService {

    @Autowired
    private GuestRepository guestRepository;

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
    public GuestResponse updateGuestStatus(String appointmentId, String guestId, GuestRequest request) {
        if (!guestRepository.existsByGuestId(guestId)) {
            throw new RuntimeException("참가자가 삭제되었거나 존재하지 않습니다. Guest ID: " + guestId);
        }
        
        // JPA Repository의 updateStatus 메서드 사용
        int updatedRows = guestRepository.updateGuestStatus(guestId, request.getGuest_status(), LocalDateTime.now());
        
        if (updatedRows == 0) {
            throw new RuntimeException("상태 업데이트에 실패했습니다. Guest ID: " + guestId);
        }
        
        // 업데이트된 데이터 조회하여 반환
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
     * Guest ID 생성 (임시)
     */
    private String generateGuestId() {
        return "guest_" + UUID.randomUUID().toString().substring(0, 8);
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
}