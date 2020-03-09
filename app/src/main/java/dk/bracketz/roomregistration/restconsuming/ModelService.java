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
    @GET("reservations")
    Call<List<Reservation>> getAllReservations();

    @POST("reservations")
    @FormUrlEncoded
    Call<Reservation> postReservation(@Field("Id") int id, @Field("FromTime") int fromTime,
                               @Field("ToTime") int integer, @Field("UserId") String userId, @Field("Purpose") String purpose, @Field("RoomId")int roomId);

    @GET("reservations/{id}")
    Call<Reservation> getOneReservation(int id);

    @DELETE("reservations/{id}")
    Call<Reservation> deleteReservation(@Path("id") int id);


    /// ROOMS
    @GET("rooms")
    Call<List<Room>> getAllRooms();
}
