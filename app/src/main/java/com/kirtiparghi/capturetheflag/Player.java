package com.kirtiparghi.capturetheflag;

/**
 * Created by kirtiparghi on 4/10/18.
 */

public class Player
{
    public String user_name;
    public String password;
    public String longitude;
    public String latitude;
    public String user_type;

    public Player(){
    }

    public Player(String user_name,String password,String longitude,String latitude,String user_type)
    {
        this.user_name=user_name;
        this.password=password;
        this.longitude = longitude;
        this.latitude=latitude;
        this.user_type=user_type;
    }
}