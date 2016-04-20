package com.nordman.big.myfellowcompass;

import java.io.Serializable;

/**
 * Created by s_vershinin on 20.04.2016.
 *
 */
public class PersonOnMap implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;

    public PersonOnMap(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
/*
        private String lat;
        private String lon;

        public PersonOnMap(String id, String lat, String lon) {
            this.id = id;
            this.lat = lat;
            this.lon = lon;
        }


        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getLon() {
            return lon;
        }

        public void setLon(String lon) {
            this.lon = lon;
        }
*/
}
