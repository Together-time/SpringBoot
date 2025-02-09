package com.tt.Together_time.domain.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sender {
    private String nickname;
    private String email;
}
