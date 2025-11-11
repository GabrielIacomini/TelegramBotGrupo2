package com.example.TelegramBotGrupo2.clients;

import com.example.TelegramBotGrupo2.dtos.BusquedaReqDTO;
import com.example.TelegramBotGrupo2.dtos.BusquedaResDto;
import com.example.TelegramBotGrupo2.dtos.ConsensoDTO;
import com.example.TelegramBotGrupo2.dtos.HechoDTO;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface AgregadorRetrofitClient {

    @GET(value = "/coleccion/{nombreColeccion}/hechos")
    Call<List<HechoDTO>> getHechosDeColeccion(@Path("nombreColeccion") String nombreColeccion);

    @PUT(value = "/consenso")
    Call<Void> cambiarConsenso(@Body ConsensoDTO consensoDTO);

    @POST(value = "/hechos/search")
    Call<BusquedaResDto> buscarHechosPorPalabras(@Body BusquedaReqDTO busquedaDto);
}
