package com.kibit.paymentapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class PaymentapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentapiApplication.class, args);
	}

}
