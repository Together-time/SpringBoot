package com.tt.Together_time.domain.dto;

import lombok.Data;

@Data
public class KakaoUserInfo {
    private Long id;
    private String connected_at;
    private Properties properties;
    private KakaoAccount kakao_account;

    @Data
    public static class Properties {
        private String nickname; // "nickname" 매핑
    }

    @Data
    public static class KakaoAccount {
        private Boolean profile_nickname_needs_agreement; // "profile_nickname_needs_agreement" 매핑
        private Profile profile; // "profile" 매핑
        private Boolean has_email; // "has_email" 매핑
        private Boolean email_needs_agreement; // "email_needs_agreement" 매핑
        private Boolean is_email_valid; // "is_email_valid" 매핑
        private Boolean is_email_verified; // "is_email_verified" 매핑
        private String email; // "email" 매핑

        @Data
        public static class Profile {
            private String nickname; // "nickname" 매핑
            private Boolean is_default_nickname; // "is_default_nickname" 매핑
        }
    }

}
