package domain.rsvp;

import domain.user.UserAPI;

import java.util.Date;

public class RSVP {
    private int RSVPId;
    private int eventId;
    private int hostUserId;
    private int guestUserId;
    private Date requestTime;
    private int status;

    public int getRSVPId() {
        return RSVPId;
    }

    public int getEventId() {
        return eventId;
    }

    public int getHostUserId() {
        return hostUserId;
    }

    public int getGuestUserId() {
        return guestUserId;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public RSVP(int RSVPId, int eventId, int hostUserId, int guestUserId, Date requestTime, int status) {
        this.RSVPId = RSVPId;
        this.eventId = eventId;
        this.hostUserId = hostUserId;
        this.guestUserId = guestUserId;
        this.requestTime = requestTime;
        this.status = status;
    }

    @Override
    public String toString() {
        return UserAPI.getUserNameByUserId(hostUserId);
    }
}
