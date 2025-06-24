package com.HATW.mapper;

import com.HATW.dto.InquiryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InquiryMapper {

    List<InquiryDTO> findAllForAdmin();

    void insertInquiry(InquiryDTO inquiry);

    void updateInquiry(InquiryDTO inquiry);

}
