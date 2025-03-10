package com.kibit.paymentapi.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TransactionListener {

    @KafkaListener(topics = "payment-notification", groupId = "transaction-notification")
    public void listen(ConsumerRecord<String, String> record) {
        System.out.println("Received notification: " + record.value());
    }
}
