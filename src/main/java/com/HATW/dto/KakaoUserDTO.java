package com.HATW.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class KakaoUserDTO {
    private String email;
    private String nickname;
}

