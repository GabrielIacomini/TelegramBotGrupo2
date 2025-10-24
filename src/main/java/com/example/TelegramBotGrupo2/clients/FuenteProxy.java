package com.example.TelegramBotGrupo2.clients;

import com.example.TelegramBotGrupo2.dtos.HechoDTO;
import com.example.TelegramBotGrupo2.dtos.PdIDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.ArrayList;
import java.util.List;

public class FuenteProxy {

    private final FuenteRetrofitClient retrofitClient;

    public FuenteProxy(String baseUrl, ObjectMapper objectMapper) {

        var retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .build();

        this.retrofitClient = retrofit.create(FuenteRetrofitClient.class);
    }

    public HechoDTO postHecho(HechoDTO hecho) {
        try {
            return retrofitClient.postHecho(hecho).execute().body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public HechoDTO getHecho(String hechoId) {
        try {
            return retrofitClient.getHecho(hechoId).execute().body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> getPdis(String hechoId)  {
        try {
            var response = retrofitClient.getPdis(hechoId).execute().body();
            if (response.size() == 0) {
                return new ArrayList<>();
            }
            return response.get(0).pdiIds;
        } catch (Exception e) {
            e.printStackTrace();
            return  new ArrayList<>();
        }
    }

    public PdIDTO postPdI(String hechoId, PdIDTO pdi) {
        try {
            return retrofitClient.postPdI(hechoId, pdi).execute().body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
