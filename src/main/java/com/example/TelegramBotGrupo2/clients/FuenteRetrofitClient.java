package com.example.TelegramBotGrupo2.clients;

import com.example.TelegramBotGrupo2.dtos.HechoDTO;
import com.example.TelegramBotGrupo2.dtos.PdIDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.List;

public interface FuenteRetrofitClient {

    @GET(value = "/hechos/{idHecho}")
    Call<HechoDTO> getHecho(@Path("idHecho") String idHecho);


    class GetPdisResponse {
        public List<String> pdiIds;
    }
    @GET(value = "/hechos/{idHecho}/pdis")
    Call<List<GetPdisResponse>> getPdis(@Path("idHecho") String idHecho);

    @POST(value = "/hechos")
    Call<HechoDTO> postHecho(@Body HechoDTO hecho);

    @POST(value = "/hechos/{idHecho}/pdis")
    Call<PdIDTO> postPdI(@Path("idHecho") String idHecho, @Body PdIDTO pdi);
}
