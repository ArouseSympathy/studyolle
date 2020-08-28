package com.studyolle.studyolle.account;

import com.studyolle.studyolle.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    //private final AuthenticationManager authenticationManager;

    @Transactional // 영속성 컨텍스트 유지하도록 만들어줌 -> 이 메소드가 진행되는 도중에 계속해서 동기화
    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        newAccount.generateEmailCheckToken();
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    private Account saveNewAccount(@Valid SignUpForm signUpForm) {
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword())) // 패스워드 암호화
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdatedByWeb(true)
                .build();
        return accountRepository.save(account);
    }

    private void sendSignUpConfirmEmail(Account newAccount) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("스터디올레, 회원 가입 인증"); //메일제목
        mailMessage.setText("/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail()); //본문
        javaMailSender.send(mailMessage);
    }

    public void login(Account account) {
        // DB에 암호화된 db밖에 접근할 수 없기 때문에 이런 방법으로 해야함
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                account.getNickname(),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // 정석적인 방법은 이것이지만 이것은 평문으로 저장된 password를 db에 저장해둬야함
        /*UsernamePasswordAuthenticationToken token1 = new UsernamePasswordAuthenticationToken(
                username, password);
        Authentication authentication = authenticationManager.authenticate(token);*/
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(token);
    }
}
