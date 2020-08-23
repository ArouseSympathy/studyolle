package com.studyolle.studyolle.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class SignUpForm {

    private final String name;
    private final String email;
    private final String password;

}
