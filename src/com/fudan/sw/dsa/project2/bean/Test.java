package com.fudan.sw.dsa.project2.bean;

public class Test {
    public static void main(String[] args) {
        Graph graph = new Graph();
        Address start = new Address("张江校区", "121.593923", "31.207892");
        Address end = new Address("人民广场", "121.478941", "31.236009");
//        ReturnValue returnValue = graph.shortestTime(start, end);
//        System.out.println(returnValue.getStartPoint().getAddress());
//        for(int i = 0; i < returnValue.getSubwayList().size(); i++)
//            System.out.println(returnValue.getSubwayList().get(i).getAddress());
//        System.out.println(returnValue.getEndPoint().getAddress());
        ReturnValue returnValue = graph.leastTransfer(start, end);
        for(int i = 0; i < returnValue.getSubwayList().size(); i++)
            System.out.println(returnValue.getSubwayList().get(i).getAddress());
    }
}
