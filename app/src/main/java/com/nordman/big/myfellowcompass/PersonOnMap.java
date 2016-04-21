package com.nordman.big.myfellowcompass;

import java.io.Serializable;

/**
 * Created by s_vershinin on 20.04.2016.
 *
 */
public class PersonOnMap implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private Double lat;
    private Double lon;

    public PersonOnMap(String id, Double lat, Double lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
    }

    public String getId() {
        return id;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }
}
