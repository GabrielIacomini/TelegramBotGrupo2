package com.example.TelegramBotGrupo2.clients;

import com.example.TelegramBotGrupo2.dtos.ConsensoDTO;
import com.example.TelegramBotGrupo2.dtos.HechoDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import java.util.List;

public interface AgregadorRetrofitClient {

    @GET(value = "/coleccion/{nombreColeccion}/hechos")
    Call<List<HechoDTO>> getHechosDeColeccion(@Path("nombreColeccion") String nombreColeccion);

    @PUT(value = "/consenso")
    Call<Void> cambiarConsenso(@Body ConsensoDTO consensoDTO);
}
