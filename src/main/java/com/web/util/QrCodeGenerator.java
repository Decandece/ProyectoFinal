package com.web.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Component
public class QrCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String TICKET_PREFIX = "TKT-";
    private static final String BAGGAGE_PREFIX = "BAG-";
    private static final String PARCEL_PREFIX = "PCL-";

    public String generateTicketQr() {
        String rawCode = generateUniqueCode(TICKET_PREFIX, 6);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(rawCode.getBytes());
    }

    public String generateBaggageTag() {
        return generateUniqueCode(BAGGAGE_PREFIX, 6);
    }

    public String generateParcelCode() {
        return generateUniqueCode(PARCEL_PREFIX, 6);
    }


    private String generateUniqueCode(String prefix, int randomDigits) {
        long timestamp = Instant.now().toEpochMilli();
        int randomBound = (int) Math.pow(10, randomDigits);
        int randomPart = RANDOM.nextInt(randomBound);
        String formatPattern = "%0" + randomDigits + "d";
        return prefix + timestamp + "-" + String.format(formatPattern, randomPart);
    }
}