package com.fudan.sw.dsa.project2.bean;


import java.util.ArrayList;

public class Line {
    private String name;
    private boolean isWalk;
    private ArrayList<Vertex> stations;
    private ArrayList<Vertex> transfer;

    public Line(String name) {
        this.name = name;
        this.isWalk = false;
        this.stations = new ArrayList<>();
        this.transfer = new ArrayList<>();
    }

    public void addStation(Vertex station) {
        this.stations.add(station);
    }

    public void addTransfer(Vertex transfer) {
        this.transfer.add(transfer);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isWalk() {
        return isWalk;
    }

    public void setWalk(boolean walk) {
        isWalk = walk;
    }

    public ArrayList<Vertex> getStations() {
        return stations;
    }

    public void setStations(ArrayList<Vertex> stations) {
        this.stations = stations;
    }

    public ArrayList<Vertex> getTransfer() {
        return transfer;
    }

    public void setTransfer(ArrayList<Vertex> transfer) {
        this.transfer = transfer;
    }

}
