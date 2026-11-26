package com.unihub.identity.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Sends transactional identity emails (captured by MailHog in local dev). */
@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	private final JavaMailSender mailSender;
	private final String fromAddress;
	private final String webOrigin;

	public EmailService(JavaMailSender mailSender, @Value("${app.mail.from:no-reply@unihub.edu.vn}") String fromAddress,
			@Value("${app.web.origin:http://localhost:3000}") String webOrigin) {
		this.mailSender = mailSender;
		this.fromAddress = fromAddress;
		this.webOrigin = webOrigin;
	}

	public void sendPasswordResetEmail(String toEmail, String fullName, String rawToken) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromAddress);
		message.setTo(toEmail);
		message.setSubject("[Unihub] Đặt lại mật khẩu tài khoản");
		message.setText("Xin chào " + fullName + ",\n\n" + "Bạn đã yêu cầu đặt lại mật khẩu tại Unihub.\n"
				+ "Mã xác nhận (hiệu lực 15 phút): " + rawToken + "\n" + "Hoặc truy cập: " + webOrigin
				+ "/reset-password?token=" + rawToken
				+ "\n\nNếu bạn không yêu cầu thao tác này, vui lòng bỏ qua email này.");
		try {
			mailSender.send(message);
		} catch (Exception ex) {
			log.warn("Failed to send password reset email to {}: {}", toEmail, ex.getMessage());
		}
	}
}
