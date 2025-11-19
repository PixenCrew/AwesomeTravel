package renewal.awesome_travel.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Async
    public void sendVerificationMail(String toEmail, String token, String endPoint) {
        String link = frontendUrl + endPoint + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[회원가입 인증] AwesomeTravel 이메일 인증");
        message.setText("아래 링크를 클릭해 이메일을 인증하세요:\n" + link);

        mailSender.send(message);
    }

    @Async
    public void sendMateMail(String toEmail, String token, String endPoint) {
        String link = frontendUrl + endPoint + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[여행메이트 등록] AwesomeTravel 이메일 인증");
        message.setText("아래 링크를 클릭해 여행메이트 등록 승인해주세요:\n" + link);

        mailSender.send(message);
    }
}
