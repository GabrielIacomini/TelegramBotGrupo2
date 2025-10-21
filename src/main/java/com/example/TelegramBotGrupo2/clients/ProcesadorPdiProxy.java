package com.example.TelegramBotGrupo2.clients;


import com.example.TelegramBotGrupo2.dtos.PdIDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

//import io.javalin.http.HttpStatus;
import lombok.SneakyThrows;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

public class ProcesadorPdiProxy {

    //private final String endpoint;
    private final ProcesadorPdiRetrofitClient service;

    public ProcesadorPdiProxy(String baseUrl, ObjectMapper objectMapper) {
        /*
        var mapperBuilder = new Jackson2ObjectMapperBuilder();
        (new JacksonConfig()).jsonCustomizer().customize(mapperBuilder);
        ObjectMapper objectMapper = mapperBuilder.build();
        */

        //var env = System.getenv();
        //this.endpoint = env.getOrDefault("URL_PROCESADOR", "https://tp-dds-2025-procesadorpdi-grupo2-1.onrender.com/api/");

        var retrofit =
                new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                        .build();

        this.service = retrofit.create(ProcesadorPdiRetrofitClient.class);
    }

    public ProcesadorPdiProxy(String endpoint, ProcesadorPdiRetrofitClient service) {
        //this.endpoint = endpoint;
        this.service = service;
    }

    public PdIDTO getPdi(String pdiId)  {
        try {
            var res = service.getPdi(pdiId).execute();
            if (!res.isSuccessful()) {
                throw new RuntimeException("Error conectandose con procesadorPdi (" + res.code() + ")");
            }

            return res.body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}