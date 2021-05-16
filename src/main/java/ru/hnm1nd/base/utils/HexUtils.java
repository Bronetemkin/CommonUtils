package ru.hnm1nd.base.utils;

public class HexUtils {

    public static String fromHex (String src) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < src.length() - 1; i += 2) {
            String tempInHex = src.substring(i, (i + 2));
            int decimal = Integer.parseInt(tempInHex, 16);
            result.append((char) decimal);
        }
        return result.toString();
    }

    public static String toHex (String src) {
        StringBuilder hex = new StringBuilder();
        for (char temp : src.toCharArray()) {
            hex.append(Integer.toHexString(temp));
        }
        return hex.toString();
    }

}
