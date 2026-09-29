package com.example.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {

        JavaMailSenderImpl mailSender =
                new JavaMailSenderImpl();

        mailSender.setHost(
                System.getenv("MAIL_HOST")
        );

        String port = System.getenv("MAIL_PORT");

        mailSender.setPort(
                port != null && !port.isBlank()
                        ? Integer.parseInt(port)
                        : 587
        );

        mailSender.setUsername(
                System.getenv("MAIL_USERNAME")
        );

        mailSender.setPassword(
                System.getenv("MAIL_PASSWORD")
        );

        Properties properties =
                mailSender.getJavaMailProperties();

        properties.put(
                "mail.transport.protocol",
                "smtp"
        );

        properties.put(
                "mail.smtp.auth",
                "true"
        );

        properties.put(
                "mail.smtp.starttls.enable",
                "true"
        );

        properties.put(
                "mail.smtp.starttls.required",
                "true"
        );

        properties.put(
                "mail.debug",
                "false"
        );

        return mailSender;
    }
}
