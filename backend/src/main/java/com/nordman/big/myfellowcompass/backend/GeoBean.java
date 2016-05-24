package com.nordman.big.myfellowcompass.backend;

/**
 * Created by s_vershinin on 14.03.2016.
 *
 */
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;

@Entity
public class GeoBean {
    @Id Long id;
    Double lat;
    Double lon;
    private String extra;


    public GeoBean() {}

    public GeoBean(Long id, Double lat, Double lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String data) {
        extra = data;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

}
