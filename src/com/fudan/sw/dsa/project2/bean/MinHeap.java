package com.fudan.sw.dsa.project2.bean;

import java.util.ArrayList;

public class MinHeap {
    public ArrayList<Vertex> vertices = new ArrayList<>();

    public MinHeap(ArrayList<Vertex> vertices) {
        this.vertices.addAll(vertices);
    }

    //得到堆的大小
    public int size() {
        return this.vertices.size();
    }

    // 构建最小堆
    public static void bulidMinHeap(MinHeap minHeap) {
        for(int i = minHeap.vertices.size() / 2; i >= 0; i--)
            minHeapify(minHeap, i);
    }

    //插入新的节点
    public static void insert(MinHeap minHeap, Vertex station) {
        int weight = station.getWeight();
        station.setWeight(Integer.MAX_VALUE);
        minHeap.vertices.add(station);
        decreaseCost(minHeap, station, weight);
    }

    public static int getParent(int index) {
        int parentIndex = index / 2;
        if(index % 2 == 0)
            parentIndex = index / 2 - 1;
        return parentIndex;
    }

    // 维护最小堆的性质
    private static void minHeapify(MinHeap minHeap, int num) {
        int leftChild = 2 * num + 1;
        int rightChild = 2 * num + 2;
        int smallest = num;
        if(leftChild < minHeap.vertices.size() && minHeap.vertices.get(leftChild).getWeight() < minHeap.vertices.get(smallest).getWeight())
            smallest = leftChild;
        if(rightChild < minHeap.vertices.size() && minHeap.vertices.get(rightChild).getWeight() < minHeap.vertices.get(smallest).getWeight())
            smallest = rightChild;
        if(smallest != num){
            exchangeElements(minHeap, num, smallest );
            minHeapify(minHeap, smallest);
        }
    }

    // 返回并删除最小节点
    public static Vertex extractMin(MinHeap minHeap) {
        Vertex min = minHeap.vertices.get(0);
        int length = minHeap.vertices.size();
        minHeap.vertices.set(0, minHeap.vertices.get(length - 1));
        minHeap.vertices.remove(length - 1);
        minHeapify(minHeap, 0);
        return min;
    }

    public static void decreaseCost(MinHeap minHeap, Vertex Address, int cost) {
        int index = minHeap.vertices.indexOf(Address);
        minHeap.vertices.get(index).setWeight(cost);
        while((index > 0) && (minHeap.vertices.get(getParent(index)).getWeight() > minHeap.vertices.get(index).getWeight())) {
            exchangeElements(minHeap, getParent(index), index);
            index = getParent(index);
        }
    }

    private static void exchangeElements(MinHeap minHeap, int num1, int num2) {
        Vertex temp = minHeap.vertices.get(num1);
        minHeap.vertices.set(num1, minHeap.vertices.get(num2));
        minHeap.vertices.set(num2, temp);
    }
}

