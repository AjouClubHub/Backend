package com.coldrice.clubing.domain.common.sms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsSender {

	private final DefaultMessageService messageService;

	@Value("${coolsms.sender.phone}")
	private String senderPhone;

	public void sendSms(String phoneNumber, String code) {
		Message message = new Message();
		message.setFrom(senderPhone);
		message.setTo(phoneNumber);
		message.setText("[Clubing] 인증번호는 [" + code + "] 입니다.");

		SingleMessageSentResponse response = messageService.sendOne(new SingleMessageSendingRequest(message));
		log.info("SMS 전송 응답: " + response);
	}
}
