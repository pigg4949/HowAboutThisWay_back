package com.HATW.service;

import com.HATW.dto.InquiryDTO;
import com.HATW.mapper.InquiryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {

    private final InquiryMapper inquiryMapper;

    @Override
    public List<InquiryDTO> findAllForAdmin() {
        return inquiryMapper.findAllForAdmin();
    }
    @Override
    public void insertInquiry(InquiryDTO inquiry) {
        inquiryMapper.insertInquiry(inquiry);
    }

    @Override
    public void updateInquiry(InquiryDTO inquiry) {
        inquiryMapper.updateInquiry(inquiry);
    }
}

