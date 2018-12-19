package com.fudan.sw.dsa.project2.bean;

public class Test {
    public static void main(String[] args) {
        Graph graph = new Graph();
        Address start = new Address("浦东新区, 环湖东一路", "121.467873", "31.414105");
        Address end = new Address("昆山市, 新联路, 128号", "121.110928", "31.30333");
//        Address start = new Address("浦东新区, 环湖东一路", "121.593923", "31.207892");
//        Address end = new Address("昆山市, 新联路, 128号", "121.521178", "31.303797");
        ReturnValue returnValue = graph.newLeastTransfer(start, end);
        for(int i = 0; i < returnValue.getSubwayList().size(); i++)
            System.out.println(returnValue.getSubwayList().get(i).getAddress());
    }
}
