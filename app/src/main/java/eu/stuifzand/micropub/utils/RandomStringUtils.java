package eu.stuifzand.micropub.utils;

import java.util.Random;
import java.util.stream.IntStream;

public class RandomStringUtils {
    public static String randomString(int n) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append((char)('a'+r.nextInt(('z'-'a')+1)));
        }
        return sb.toString();
    }
}
