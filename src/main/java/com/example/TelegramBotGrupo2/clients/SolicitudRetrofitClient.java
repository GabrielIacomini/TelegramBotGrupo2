package com.example.TelegramBotGrupo2.clients;

import com.example.TelegramBotGrupo2.dtos.SolicitudDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.PATCH;

public interface SolicitudRetrofitClient {
    @POST("/solicitudes")
    Call<SolicitudDto> agregarSolicitud(@Body SolicitudDto solicitud);

    @PATCH("/solicitudes")
    Call<SolicitudDto> actualizarSolicitud(@Body SolicitudDto solicitud);

    @GET("/solicitudes/{id}")
    Call<SolicitudDto> consultarSolicitud(@Path("id") int id);
}
