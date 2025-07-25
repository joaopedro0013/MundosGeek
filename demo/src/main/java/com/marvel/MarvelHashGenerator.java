package com.marvel;

import java.security.MessageDigest;

public class MarvelHashGenerator {

    public static void main(String[] args) {
        String publicKey = "f271c01e016bcfff110aa0036fceb53e";
        String privateKey = "d8748a7e0aced5c3dcc0e5650f16dfb49d452418";
        String ts = String.valueOf(System.currentTimeMillis());

        String hash = generateMarvelHash(ts, privateKey, publicKey);

        System.out.println("ts: " + ts);
        System.out.println("hash: " + hash);
        System.out.println("apikey: " + publicKey);
    }

    public static String generateMarvelHash(String ts, String privateKey, String publicKey) {
        try {
            String value = ts + privateKey + publicKey;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(value.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar hash MD5", e);
        }
    }
}