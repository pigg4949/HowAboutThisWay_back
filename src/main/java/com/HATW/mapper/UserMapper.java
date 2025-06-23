// src/main/java/com/koreait/hatw_b/mapper/UserMapper.java
package com.HATW.mapper;

import com.HATW.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * UserMapper: Java 메서드 호출을 SQL로 매핑해 주는 MyBatis 인터페이스
 */
@Mapper
public interface UserMapper {

    /**
     * 전체 사용자 조회 (관리자용)
     */
    List<UserDTO> findAll();

    /**
     * 단일 사용자 조회
     * @param userId 조회할 사용자 아이디
     */
    UserDTO findByUserId(@Param("userId") String userId);

    /**
     * 새 사용자 등록
     * useGeneratedKeys=true: DB가 만든 PK(idx)를 DTO.idx에 채워줌
     */
    void insertUser(UserDTO user);

    /**
     * 사용자 정보 수정
     * @param user 수정할 사용자 데이터 (userId 기준)
     */
    void updateUser(UserDTO user);

    /**
     * 사용자 삭제
     * @param userId 삭제할 사용자 아이디
     */
    void deleteByUserId(@Param("userId") String userId);
}
