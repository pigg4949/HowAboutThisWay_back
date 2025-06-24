// src/main/java/com/koreait/hatw_b/mapper/MarkerMapper.java
package com.HATW.mapper;

import com.HATW.dto.MarkerDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MarkerMapper: 단순히 마커 띄워주는 DTO긴 한데,, 혹시 몰라서 일단 여러 개 만들어 놨고, 필요 없으면 타입 빼고 지우면 됩니다.
 */
@Mapper
public interface MarkerMapper {
    List<MarkerDTO> findMarkersByTypes(@Param("types") List<Integer> types);

}
