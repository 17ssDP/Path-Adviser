package com.fudan.sw.dsa.project2.bean;

import com.fudan.sw.dsa.project2.constant.FileGetter;
//import javafx.geometry.VerticalDirection;
//import sun.jvm.hotspot.opto.MachIfNode;
//import jdk.internal.module.SystemModuleFinders;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * for subway graph
 * @author dengpeng
 *
 */
public class Graph {
    private static final int DIMENSION = 2;  //KdTree维度
    private static final int SPEED = 5;  //步行速度
    private ArrayList<Vertex> stationsList;
    private MyKdTree<Vertex> stations;

    public Graph() {
        this.stations = new MyKdTree<>(DIMENSION);
        this.stationsList = new ArrayList<>();
        makeGraph();
    }

    private void makeGraph() {
        FileGetter fileGetter = new FileGetter();
        File file = fileGetter.readFileFromClasspath();
        DataBuilder dataBuilder = new DataBuilder();
        dataBuilder.loadMap(file, stationsList, stations);
    }

    //Find the shortest time path from start to end
    private void Dijkstra(Vertex start, Vertex end) {
        initialize();
        MinHeap minHeap = new MinHeap(this.stationsList);
        initialize(start);
        MinHeap.bulidMinHeap(minHeap);
        while(minHeap.size() > 0) {
            Vertex minVertex = MinHeap.extractMin(minHeap);
            for(int i = 0; i < minVertex.getAdj().size(); i++) {
                relax(minVertex, minVertex.getAdj().get(i), minVertex.getAdj().get(i).getTime(), minHeap);
            }
            if(minVertex == end)
                break;
        }
    }

    //Find the shortest time path
    public ReturnValue shortestTime(Address start, Address end) {
        ResultHeap<Vertex> startNearest = stations.getNearestNeighbors(new double[] {start.getLongitude(), start.getLatitude()}, 5);
        ResultHeap<Vertex> endNearest = stations.getNearestNeighbors(new double[] {end.getLongitude(), end.getLatitude()}, 5);
        ReturnValue returnValue = new ReturnValue();
        returnValue.setMinutes(Double.MAX_VALUE);
        for(int i = 0; i < startNearest.size(); i++) {
            for(int j = 0; j < endNearest.size(); j++) {
                Dijkstra(startNearest.getObject(i), endNearest.getObject(j));
                ReturnValue temp = getReturn(start, end, startNearest.getObject(i), endNearest.getObject(j));
                if(temp.getMinutes() < returnValue.getMinutes())
                    returnValue = temp;
            }
        }
        return returnValue;
    }

    //Find the walking least path
    public ReturnValue leastWalk(Address start, Address end) {
        ResultHeap<Vertex> startHeap = stations.getNearestNeighbors(new double[] {start.getLongitude(), start.getLatitude()}, 1);
        ResultHeap<Vertex> endHeap = stations.getNearestNeighbors(new double[] {end.getLongitude(), end.getLatitude()}, 1);
        Dijkstra(startHeap.getObject(0), endHeap.getObject(0));
        return  getReturn(start, end, startHeap.getObject(0), endHeap.getObject(0));
    }

    //Find the least transfer path
    public void leastTransfer(Address start, Address end) {
        ResultHeap<Vertex> startNearest = stations.getNearestNeighbors(new double[] {start.getLongitude(), start.getLatitude()}, 5);
        ResultHeap<Vertex> endNearest = stations.getNearestNeighbors(new double[] {end.getLongitude(), end.getLatitude()}, 5);
//        for(int i = 0; i < startNearest.size(); i++) {
//            for(int j = 0; j < endNearest.size(); j++) {
//                anotherNoName(startNearest.get);
//            }
//        }
        System.out.println(stationsList.indexOf(startNearest.getObject(0)));
        initialize(startNearest.getObject(0));
        anotherNoName(startNearest.getObject(0), 0);
        Vertex endStation = endNearest.getObject(0);
        while(endStation != startNearest.getObject(0)) {
            System.out.println(endStation.getAddress() + " " + endStation.getPreList().size());
            endStation = endStation.getPre().getStart();
        }
    }

    //Find the least transfer path from start to end
    private void NoName(ArrayList<Vertex> stations, Vertex start, Vertex end) {
        //0次换乘
        boolean arrive = transferOnce(start, end);
        //1次换乘
        if(!arrive) {
            for(int i = 0; i < start.getLines().size(); i++) {
                Line line = start.getLines().get(i);
                for(int j = 0; j < line.getTransfer().size(); j++) {
                    Vertex station = line.getTransfer().get(j);
                    arrive =  transferOnce(station, end);
                    if(arrive) {
                        constructLine(start, station, i);
                    }
                }
            }
        }

        //2次换乘
        if(!arrive) {
            for(int i = 0; i < start.getLines().size(); i++) {
                Line line = start.getLines().get(i);
                for(int j = 0; j < line.getTransfer().size(); j++) {
                    Vertex station = line.getTransfer().get(j);
                    for(int k = 0; k < station.getLines().size(); k++) {
                        Line subLine = station.getLines().get(k);
                        for(int n = 0; n < subLine.getTransfer().size(); n++) {
                            Vertex subStation = subLine.getTransfer().get(n);
                            arrive = transferOnce(subStation, end);
                            if(arrive) {
                                constructLine(station, subStation, k);
                                constructLine(start, station, i);
                            }
                        }
                    }
                }
            }
        }
        //
    }

    //Another way to find the least transfer path from start to end
    private void anotherNoName(Vertex start, int weight) {
        boolean over = true;
        for(int i = 0; i < start.getLines().size(); i++) {
            Line line = start.getLines().get(i);
            if(!line.isWalk()) {
                over = false;
                line.setWalk(true);
                int index = line.getStations().indexOf(start);
                for(int j = index - 1; j >= 0; j--) {
                    Vertex station = line.getStations().get(j);
                    if(line.getStations().get(j).getWeight() == line.getStations().get(j + 1).getWeight()) {
                        line.getStations().get(j).getPreList().add(getEdge(line, line.getStations().get(j + 1), line.getStations().get(j)));
                    }
                    if(line.getStations().get(j).getWeight()  > line.getStations().get(j + 1).getWeight()) {
                        line.getStations().get(j).setPre(getEdge(line, line.getStations().get(j + 1), line.getStations().get(j)));
                        line.getStations().get(j).setWeight(weight);
                    }
                }
                for(int j = index + 1; j < line.getStations().size(); j++) {
                    Vertex station = line.getStations().get(j);
                    if(line.getStations().get(j).getWeight() == line.getStations().get(j - 1).getWeight()) {
                        line.getStations().get(j).getPreList().add(getEdge(line, line.getStations().get(j - 1), line.getStations().get(j)));
                    }
                    if(line.getStations().get(j).getWeight()  > line.getStations().get(j - 1).getWeight()) {
                        line.getStations().get(j).setPre(getEdge(line, line.getStations().get(j - 1), line.getStations().get(j)));
                        line.getStations().get(j).setWeight(weight);
                    }
                }
            }
        }
        if(!over) {
            weight++;
            for(int i = 0; i < start.getLines().size(); i++) {
                Line line = start.getLines().get(i);
                for(int j = 0; j < line.getTransfer().size(); j++) {
                    anotherNoName(line.getTransfer().get(j), weight);
                }
            }
        }
    }

    private void addLine(ArrayList<Line> lines, Vertex station) {

    }

    private boolean haveSameLine(ArrayList<Line> lines, Vertex end) {
        for (Line line : lines) {
            for (int j = 0; j < end.getLines().size(); j++) {
                if (line.getName().equals(end.getLines().get(j).getName()))
                    return true;
            }
        }
        return false;
    }

    private void constructLine(Vertex start, Vertex end, int i) {
        Line line = start.getLines().get(i);
        constructPath(line, start, end);
    }

    private void constructPath(Line line, Vertex start, Vertex end) {
        int startNum = line.getStations().indexOf(start);
        int endNum = line.getStations().indexOf(end);
        if(startNum < endNum) {
            for(int i = endNum; i > startNum; i--) {
                Edge edge = getEdge(line, line.getStations().get(i - 1), end);
                line.getStations().get(i).setPre(edge);
                end = edge.getStart();
            }
        }else if(startNum > endNum) {
            for(int i = endNum; i < startNum; i++) {
                Edge edge = getEdge(line, line.getStations().get(i + 1), end);
                line.getStations().get(i).setPre(edge);
                end = edge.getStart();
            }
        }
    }

    private Edge getEdge(Line line, Vertex start, Vertex end) {
        for(int j = 0; j < start.getAdj().size(); j++)
            if(start.getAdj().get(j).getLine().equals(line.getName()) && start.getAdj().get(j).getEnd() == end)
                return start.getAdj().get(j);
        return null;
    }

    //一次换乘可达
    private boolean transferOnce(Vertex start, Vertex end) {
        boolean once = false;
        for(int i = 0; i < start.getLines().size(); i++) {
            for(int j = 0; j < end.getLines().size(); j++) {
                if(start.getLines().get(i) == end.getLines().get(j)) {
                    once = true;
                    constructLine(start, end, i);
                }
            }
        }
        return once;
    }

    //初始化站点的权重
    private void initialize(Vertex start) {
        for(Vertex aVertex : stationsList) {
            aVertex.setWeight(Integer.MAX_VALUE);
            aVertex.setPre(null);
        }
        start.setWeight(0);
    }

    //初始化站点的先驱链表
    private void initialize() {
        for (Vertex vertex : stationsList) {
            vertex.getPreList().clear();
        }
    }

    private void relax(Vertex start, Edge edge, int time, MinHeap minHeap) {
        Vertex end = edge.getEnd();
        if(end.getWeight() > start.getWeight() + time) {
            MinHeap.decreaseCost(minHeap, end, start.getWeight() + time);
            end.setPre(edge);
            end.getPreList().clear();
        } else if(end.getWeight() == start.getWeight() + time) {
            end.getPreList().add(edge);
        }
//        if(end.getWeight() == start.getWeight() && end.getPre() != null && end != start.getPre().getStart()) {
////            Vertex cloneStation = new Vertex(end);
////            cloneStation.setPre(edge);
////            MinHeap.insert(minHeap, cloneStation);
//        }
//        if(end.getWeight() > start.getWeight()) {
//            if(end.isTransfer(start, edge))
//                MinHeap.decreaseCost(minHeap, end, start.getWeight() + 1);
//            else
//                MinHeap.decreaseCost(minHeap, end, start.getWeight());
//            end.setPre(edge);
//        }
    }

    //生成返回值
    private ReturnValue getReturn(Address start, Address end, Vertex startStation, Vertex endStation) {
        ReturnValue returnValue = new ReturnValue();
        //设置起点和终点
        returnValue.setStartPoint(start);
        returnValue.setEndPoint(end);
        //添加中间站点
        List<Address> subwayList = new ArrayList<>();
        while (endStation != startStation) {
            subwayList.add(0, new Address(endStation.getAddress(),
                    Double.toString(endStation.getCoords()[0]), Double.toString(endStation.getCoords()[1])));
            endStation = endStation.getPre().getStart();
        }
        subwayList.add(0, new Address(startStation.getAddress(),
                Double.toString(startStation.getCoords()[0]), Double.toString(startStation.getCoords()[1])));
        returnValue.setSubwayList(subwayList);
        //计算时间
        double time = calculateTime(start, end, startStation, endStation);
        returnValue.setMinutes(time);
        return returnValue;
    }

    //计算路程所花费的时间
    private double calculateTime(Address start, Address end, Vertex startStation, Vertex endStation) {
        double distant1 = startStation.distanceTo(start);
        double distant2 = endStation.distanceTo(end);
        return distant1 / SPEED + endStation.getWeight() + distant2 / SPEED;
    }

    public ArrayList<Vertex> getStationsList() {
        return stationsList;
    }

    public void setStationsList(ArrayList<Vertex> stationsList) {
        this.stationsList = stationsList;
    }

    public MyKdTree<Vertex> getStations() {
        return stations;
    }

    public void setStations(MyKdTree<Vertex> stations) {
        this.stations = stations;
    }
}
