package com.kirtiparghi.capturetheflag;

import java.io.Serializable;

/**
 * Created by kirtiparghi on 4/10/18.
 */

public class Player implements Serializable {
    public String playerId;
    public String player;
    public String passcode;
    public String team;
    public String longitude;
    public String latitude;
    public String haveFlag;

    // constructors
    public Player() {
        // leave this constructor empty
    }

    public Player(String playerId,String player, String passcode, String team,String longitude,String latitude, String haveFlag) {
        this.playerId=playerId;
        this.player = player;
        this.passcode = passcode;
        this.team = team;
        this.longitude=longitude;
        this.latitude=latitude;
        this.haveFlag = haveFlag;
    }

    public String getHaveFlag() {
        return haveFlag;
    }

    public void setHaveFlag(String haveFlag) {
        this.haveFlag = haveFlag;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getPasscode() {
        return passcode;
    }

    public void setPasscode(String passcode) {
        this.passcode = passcode;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
}