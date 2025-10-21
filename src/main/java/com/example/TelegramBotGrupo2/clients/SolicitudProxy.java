package com.example.TelegramBotGrupo2.clients;

import com.example.TelegramBotGrupo2.dtos.HechoDTO;
import com.example.TelegramBotGrupo2.dtos.SolicitudDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.ArrayList;
import java.util.List;

public class SolicitudProxy {

    private final SolicitudRetrofitClient retrofitClient;

    public SolicitudProxy(String baseUrl, ObjectMapper objectMapper) {

        var retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .build();

        this.retrofitClient = retrofit.create(SolicitudRetrofitClient.class);
    }

    public SolicitudDto patchSolicitud(SolicitudDto solic) {
        try {
            return retrofitClient.actualizarSolicitud(solic).execute().body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public SolicitudDto postSolicitud(SolicitudDto solic) {
        try {
            return retrofitClient.agregarSolicitud(solic).execute().body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public SolicitudDto getSolicitud(int id) {
        try {
            return retrofitClient.consultarSolicitud(id).execute().body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
