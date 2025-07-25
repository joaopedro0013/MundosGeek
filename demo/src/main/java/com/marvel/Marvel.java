package com.marvel;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.security.MessageDigest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.google.gson.*;

public class Marvel {

    public static String buscarPersonagemPorNome(String nome) {
        String publicKey = "ca1aab755bd9ad4427da90b8d8daaee5";
        String privateKey = "1b4d707525f19537e17b9dc9005ba14671e17ef3";
        String ts = String.valueOf(System.currentTimeMillis());
        try {
            String hash = md5(ts + privateKey + publicKey);
            String nomeUrl = URLEncoder.encode(nome, StandardCharsets.UTF_8);
            String urlChar = "https://gateway.marvel.com/v1/public/characters?name=" + nomeUrl +
                    "&ts=" + ts + "&apikey=" + publicKey + "&hash=" + hash;
            OkHttpClient client = new OkHttpClient();
            Request requestChar = new Request.Builder().url(urlChar).build();
            try (Response responseChar = client.newCall(requestChar).execute()) {
                return responseChar.body().string();
            }
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    public static String buscarComicsPorPersonagem(String nome) {
        String publicKey = "ca1aab755bd9ad4427da90b8d8daaee5";
        String privateKey = "1b4d707525f19537e17b9dc9005ba14671e17ef3";
        String ts = String.valueOf(System.currentTimeMillis());
        try {
            String hash = md5(ts + privateKey + publicKey);
            String nomeUrl = URLEncoder.encode(nome, StandardCharsets.UTF_8);
            // Busca o personagem para pegar o ID
            String urlChar = "https://gateway.marvel.com/v1/public/characters?name=" + nomeUrl +
                    "&ts=" + ts + "&apikey=" + publicKey + "&hash=" + hash;
            OkHttpClient client = new OkHttpClient();
            Request requestChar = new Request.Builder().url(urlChar).build();
            try (Response responseChar = client.newCall(requestChar).execute()) {
                String jsonChar = responseChar.body().string();
                JsonObject root = JsonParser.parseString(jsonChar).getAsJsonObject();
                JsonArray results = root.getAsJsonObject("data").getAsJsonArray("results");
                if (results.size() > 0) {
                    JsonObject personagem = results.get(0).getAsJsonObject();
                    int charId = personagem.get("id").getAsInt();
                    // Agora busca os comics desse personagem
                    String urlComics = "https://gateway.marvel.com/v1/public/characters/" + charId + "/comics?limit=100"
                            + "&ts=" + ts + "&apikey=" + publicKey + "&hash=" + hash;
                    Request requestComics = new Request.Builder().url(urlComics).build();
                    try (Response responseComics = client.newCall(requestComics).execute()) {
                        return responseComics.body().string();
                    }
                } else {
                    return "{\"data\":{\"results\":[]}}";
                }
            }
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    public static String md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : messageDigest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}