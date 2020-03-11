package dk.bracketz.roomregistration.restconsuming;

public class ApiUtils {
    private static final String BASE_URL = "http://anbo-roomreservationv3.azurewebsites.net/api/";

    public static ModelService getReservationService() {

        return RetrofitClient.getClient(BASE_URL).create(ModelService.class);
    }
}
