package com.HATW.mapper;

import com.HATW.dto.ReportDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReportMapper {
    List<ReportDTO> findApprovedReports();
}
