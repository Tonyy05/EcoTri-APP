package iut.dam.ecotri.data;

import iut.dam.ecotri.data.BanApi;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BanClient {
    private static final String BASE_URL = "https://api-adresse.data.gouv.fr/";
    private static BanApi api;

    public static BanApi getApi() {
        if (api == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            api = retrofit.create(BanApi.class);
        }
        return api;
    }
}
