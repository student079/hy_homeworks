package domain.rsvp;

import domain.user.UserAPI;

import java.util.Date;

public class HostRSVP extends RSVP {

    public HostRSVP(int RSVPId, int eventId, int hostUserId, int guestUserId, Date requestTime, int status) {
        super(RSVPId, eventId, hostUserId, guestUserId, requestTime, status);
    }

    @Override
    public String toString() {
        return UserAPI.getUserNameByUserId(getGuestUserId());
    }
}
