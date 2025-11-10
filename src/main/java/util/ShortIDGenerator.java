package main.java.util;

import main.java.infra.config.Config;
import java.security.SecureRandom;

public class ShortIDGenerator {
    private static final String ALPHABET = Config.getGeneratorAlphabet();
    private static final int GEN_LENGTH = Config.getGeneratorLength();
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateShortID() {
        StringBuilder sb = new StringBuilder(GEN_LENGTH);

        for (int i = 0; i < GEN_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }

        return sb.toString();

    }
}
