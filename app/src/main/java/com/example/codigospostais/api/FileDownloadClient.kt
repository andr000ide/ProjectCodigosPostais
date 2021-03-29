package com.example.codigospostais.api
import retrofit2.Call;
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming

interface FileDownloadClient {
    //@GET("blob/master/data/codigos_postais.csv")
    @Streaming
    @GET("master/data/codigos_postais.csv")
    fun downloadFile() : Call<ResponseBody>
}