package com.fudan.sw.dsa.project2.bean;

import java.util.ArrayList;

public class Vertex {
    private static final int K = 2;
    private static final double EARTH_RADIUS = 6371.393;
    private String address;
    private double[] coords = new double[K]; //coords[0] = longitude, coords[1] = latitude
    private ArrayList<Edge> adj;
    private Edge pre;
    private ArrayList<Edge> preList;
    private ArrayList<Line> lines;
    private int weight;
    private int transfer;
    public Vertex(String address,double longitude, double latitude) {
        this.address = address;
        this.coords[0] = longitude;
        this.coords[1] = latitude;
        this.adj = new ArrayList<>();
        this.pre = null;
        this.preList = new ArrayList<>();
        this.lines = new ArrayList<>();
        this.weight = Integer.MAX_VALUE;
    }

    public Vertex(double[] vals) {
        this.coords[0] = vals[0];
        this.coords[1] = vals[1];
    }

    public Vertex(double longitude, double latitude) {
        this.coords[0] = longitude;
        this.coords[1] = latitude;
    }

    public Vertex(Vertex address) {
        this.address = address.address;
        this.coords[0] = address.coords[0];
        this.coords[1] = address.coords[1];
        this.adj = address.adj;
        this.pre = address.pre;
        this.preList = address.preList;
        this.weight = address.weight;
    }

    public double distanceTo(Address address) {
        double radLat1 = Math.toRadians(this.coords[1]);
        double radLat2 = Math.toRadians(address.getLatitude());
        double angle1 = radLat1 - radLat2;
        double angle2 = Math.toRadians(this.coords[0] - address.getLongitude());
        double dis = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(angle1 / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(angle2 / 2), 2)));
        dis *= EARTH_RADIUS;
        dis = Math.round(dis * 1000);
        return dis;
    }

    public boolean isTransfer(Vertex start, Edge edge) {
        if(start.getPre() == null)
            return false;
        else {
            String line = start.getPre().getLine();
            return !line.equals(edge.getLine());
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ArrayList<Edge> getAdj() {
        return adj;
    }

    public void setAdj(ArrayList<Edge> adj) {
        this.adj = adj;
    }

    public Edge getPre() {
        return pre;
    }

    public void setPre(Edge pre) {
        this.pre = pre;
    }

    public ArrayList<Edge> getPreList() {
        return preList;
    }

    public void setPreList(ArrayList<Edge> preList) {
        this.preList = preList;
    }

    public double[] getCoords() {
        return coords;
    }

    public void setCoords(double[] coords) {
        this.coords = coords;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public ArrayList<Line> getLines() {
        return lines;
    }

    public void setLines(ArrayList<Line> lines) {
        this.lines = lines;
    }

    public int getTransfer() {
        return transfer;
    }

    public void setTransfer(int transfer) {
        this.transfer = transfer;
    }

}
