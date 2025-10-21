package com.example.TelegramBotGrupo2.clients;

import com.example.TelegramBotGrupo2.dtos.HechoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AgregadorProxy {

    private final AgregadorRetrofitClient agregadorRetrofitClient;

    public AgregadorProxy(String baseUrl, ObjectMapper objectMapper) {

        var retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .build();

        this.agregadorRetrofitClient = retrofit.create(AgregadorRetrofitClient.class);
    }

    public List<HechoDTO> getHechos(String nombreColeccion) {
        try {
            return agregadorRetrofitClient.getHechosDeColeccion(nombreColeccion).execute().body();
        } catch (Exception e) {
            e.printStackTrace();
            return  new ArrayList<>();
        }
    }

}
