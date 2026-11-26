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
		sendSafely(message, toEmail);
	}

	public void sendAdminResetPasswordEmail(String toEmail, String fullName, String temporaryPassword) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromAddress);
		message.setTo(toEmail);
		message.setSubject("[Unihub] Mật khẩu tài khoản đã được đặt lại");
		message.setText("Xin chào " + fullName + ",\n\n"
				+ "Quản trị viên đã đặt lại mật khẩu cho tài khoản Unihub của bạn.\n" + "Mật khẩu tạm thời: "
				+ temporaryPassword + "\nVui lòng đăng nhập và đổi mật khẩu ngay sau khi truy cập.");
		sendSafely(message, toEmail);
	}

	private void sendSafely(SimpleMailMessage message, String toEmail) {
		try {
			mailSender.send(message);
		} catch (Exception ex) {
			log.warn("Failed to send email to {}: {}", toEmail, ex.getMessage());
		}
	}
}
