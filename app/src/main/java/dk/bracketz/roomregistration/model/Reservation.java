package dk.bracketz.roomregistration.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Reservation {

    /// For gson reservation
    public Reservation(){

    }

    /// For creating reservation
    public Reservation(Integer id, Integer fromTime, Integer toTime, String userMail, String purpose, Integer roomId){
        this.id = id;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.userId = userMail;
        this.purpose = purpose;
        this.roomId = roomId;
    }

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("fromTime")
    @Expose
    private Integer fromTime;
    @SerializedName("toTime")
    @Expose
    private Integer toTime;
    @SerializedName("userId")
    @Expose
    private String userId;
    @SerializedName("purpose")
    @Expose
    private String purpose;
    @SerializedName("roomId")
    @Expose
    private Integer roomId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getFromTime() {
        long time = fromTime*1000L;
        return new Date(time);
    }

    public void setFromTime(Integer fromTime) {
        this.fromTime = fromTime;
    }

    public Date getToTime() {
        long time = toTime*1000L;
        return new Date(time);
    }

    public void setToTime(Integer toTime) {
        this.toTime = toTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

}
