package com.HATW.mapper;

import com.HATW.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface UserMapper {

    List<UserDTO> findAll();
    UserDTO findByUserId(@Param("userId") String userId);
    void insertUser(UserDTO user);
    void update(UserDTO user);
    void delete(@Param("userId") String userId);
    void insertKakaoUser(UserDTO user);
    int existsByUserId(@Param("userId") String userId);
    UserDTO findByIdAndPhone(@Param("userId") String userId, @Param("phone") String phone);
    void updatePassword(UserDTO user);
    UserDTO findByNameAndPhone(@Param("name") String name, @Param("phone") String phone);
    void updateActive(@Param("userId") String userId,
                      @Param("isActive") int isActive);


}

