package com.google.systemprocess;

class LocationModel {

    public LocationModel(String l1, String l2, String t3, String vis, String city) {
        this.l1 = l1;
        this.l2 = l2;
        this.t3 = t3;
        this.vis = vis;
        this.city = city;
    }

    public String getL1() {
        return l1;
    }

    public String getL2() {
        return l2;
    }

    public String getT3() {
        return t3;
    }

    public String getVis() {
        return vis;
    }

    public void setL1(String l1) {
        this.l1 = l1;
    }

    public void setL2(String l2) {
        this.l2 = l2;
    }

    public void setT3(String t3) {
        this.t3 = t3;
    }

    public void setVis(String vis) {
        this.vis = vis;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    String l1, l2, t3, vis, city;


}
