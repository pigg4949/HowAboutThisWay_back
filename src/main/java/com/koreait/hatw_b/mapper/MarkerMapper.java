// src/main/java/com/koreait/hatw_b/mapper/MarkerMapper.java
package com.koreait.hatw_b.mapper;

import com.koreait.hatw_b.dto.MarkerDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MarkerMapper: 단순히 마커 띄워주는 DTO긴 한데,, 혹시 몰라서 일단 여러 개 만들어 놨고, 필요 없으면 타입 빼고 지우면 됩니다.
 */
@Mapper
public interface MarkerMapper {

    /**
     * 타입별 마커 조회
     * @param type 마커 타입
     */
    List<MarkerDTO> findByType(@Param("type") int type);

    /**
     * 전체 마커 조회 (관리자용)
     */
    List<MarkerDTO> findAll();

    /**
     * 단일 마커 조회
     * @param idx 마커 PK
     */
    MarkerDTO findByIdx(@Param("idx") int idx);

    /**
     * 새 마커 등록
     * useGeneratedKeys=true 로 DB 생성 PK(idx)를 DTO.idx에 채워줌
     */
    void insertMarker(MarkerDTO marker);

    /**
     * 마커 정보 수정
     * @param marker idx, type, lon, lat, address, comment, weight 포함
     */
    void updateMarker(MarkerDTO marker);

    /**
     * 마커 삭제
     * @param idx 삭제할 마커 PK
     */
    void deleteMarker(@Param("idx") int idx);
}
