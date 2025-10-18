package com.example.TelegramBotGrupo2.clients;

import com.example.TelegramBotGrupo2.dtos.SolicitudDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PATCH;

public interface SolicitudRetrofitClient {
    @POST("/api/solicitudes")
    Call<SolicitudDto> agregarSolicitud(@Body SolicitudDto solicitud);

    @PATCH("/api/solicitudes")
    Call<SolicitudDto> actualizarSolicitud(@Body SolicitudDto solicitud);
}
