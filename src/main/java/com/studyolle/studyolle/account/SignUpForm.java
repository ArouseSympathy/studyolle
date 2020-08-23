package com.studyolle.studyolle.account;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public class SignUpForm {

    private String nickname;
    private String email;
    private String password;

}
