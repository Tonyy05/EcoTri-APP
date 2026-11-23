package iut.dam.ecotri.data;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BanApi {
    @GET("search/")
    Call<BanResponse> search(@Query("q") String q, @Query("limit") int limit);
}
