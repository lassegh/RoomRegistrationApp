package dk.bracketz.roomregistration.restconsuming;

import java.util.List;

import dk.bracketz.roomregistration.model.Reservation;
import dk.bracketz.roomregistration.model.Room;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ModelService {

    /// RESERVATIONS

    // get reservations from specific room
    @GET("reservations/room/{ids}/{fromTime}/{toTime}")
    Call<List<Reservation>> getAllReservations(@Path("ids") int ids,@Path("fromTime") int fromTime,@Path("toTime") int toTime);

    @POST("reservations")
    //@FormUrlEncoded
    Call<Integer> postReservation(@Body Reservation reservation);

    @GET("reservations/{id}")
    Call<Reservation> getOneReservation(int id);

    @DELETE("reservations/{id}")
    Call<Void> deleteReservation(@Path("id") int id);

    /// ROOMS
    @GET("rooms")
    Call<List<Room>> getAllRooms();

    @GET("rooms/free/{fromTime}")
    Call<List<Room>> getAvailableRooms(@Path("fromTime") int fromTime);

}
