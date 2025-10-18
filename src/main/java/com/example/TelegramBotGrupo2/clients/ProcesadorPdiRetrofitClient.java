package com.example.TelegramBotGrupo2.clients;
import com.example.TelegramBotGrupo2.dtos.PdIDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Body;
import retrofit2.http.Path;

public interface ProcesadorPdiRetrofitClient {

    @GET("pdis/{pdiId}")
    Call<PdIDTO> getPdi(@Path("pdiId") String pdiId);
}