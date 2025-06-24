package com.HATW.controller;

import com.HATW.dto.InquiryDTO;
import com.HATW.dto.UserDTO;
import com.HATW.service.InquiryService;
import com.HATW.service.UserService;
import com.HATW.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    // 관리자용 조회
    @GetMapping("/admin")
    public ResponseEntity<List<InquiryDTO>> getAllForAdmin() {
        List<InquiryDTO> inquiries = inquiryService.findAllForAdmin();
        if (inquiries.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(inquiries);
    }
    @PostMapping
    public ResponseEntity<Void> createInquiry(
            @RequestHeader("Authorization") String token,
            @RequestBody InquiryDTO inquiryDTO) {
        UserDTO user = userService.getUserInfoFromToken(token);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        inquiryDTO.setUserId(user.getUserId());
        inquiryService.insertInquiry(inquiryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 문의 답변
     * PUT /api/inquiries/{idx}
     */
    @PutMapping("/{idx}")
    public ResponseEntity<Void> updateInquiry(
            @PathVariable int idx,
            @RequestBody InquiryDTO inquiryDTO) {
        inquiryDTO.setIdx(idx);
        inquiryService.updateInquiry(inquiryDTO);
        return ResponseEntity.ok().build();
    }

}
