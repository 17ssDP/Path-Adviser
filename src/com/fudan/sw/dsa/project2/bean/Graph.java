package com.fudan.sw.dsa.project2.bean;

import com.fudan.sw.dsa.project2.constant.FileGetter;
//import sun.jvm.hotspot.runtime.VMReg;
//import javafx.geometry.VerticalDirection;
//import sun.jvm.hotspot.opto.MachIfNode;
//import jdk.internal.module.SystemModuleFinders;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * for subway graph
 * @author dengpeng
 *
 */
public class Graph {
    private static final int DIMENSION = 2;  //KdTree维度
    private static final int SPEED = 5;  //步行速度
    private static final int NUM = 5;
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
        ResultHeap<Vertex> startNearest = stations.getNearestNeighbors(new double[] {start.getLongitude(), start.getLatitude()}, NUM);
        ResultHeap<Vertex> endNearest = stations.getNearestNeighbors(new double[] {end.getLongitude(), end.getLatitude()}, NUM);
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
        //System.out.println(returnValue.getMinutes());
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
        ResultHeap<Vertex> startNearest = stations.getNearestNeighbors(new double[] {start.getLongitude(), start.getLatitude()}, NUM);
        ResultHeap<Vertex> endNearest = stations.getNearestNeighbors(new double[] {end.getLongitude(), end.getLatitude()}, NUM);
        System.out.println(stationsList.indexOf(startNearest.getObject(0)));
        System.out.println(stationsList.indexOf(endNearest.getObject(0)));
        ArrayList<Vertex> vertices = new ArrayList<>();
        ArrayList<Line> lines = new ArrayList<>();
        for(int i = 0; i < startNearest.getObject(0).getLines().size(); i++) {
            vertices.add(startNearest.getObject(0));
            lines.add(startNearest.getObject(0).getLines().get(i));
        }
        initialize(startNearest.getObject(0));
        anotherNoName(vertices, lines, 0);
        Vertex endStation = endNearest.getObject(0);
        String tempLine = "";
        while(endStation != startNearest.getObject(0)) {
            if(endStation.getPreList().size() > 0) {
                for(int i = 0; i < endStation.getPreList().size(); i++) {
                    if(endStation.getPreList().get(i).getLine().equals(tempLine))
                        endStation.setPre(endStation.getPreList().get(i));
                }
            }
            System.out.println(endStation.getAddress() + " " + endStation.getPreList().size() + " " + endStation.getWeight());
            tempLine = endStation.getPre().getLine();
            endStation = endStation.getPre().getStart();
        }
//        for(int i = 0; i < startNearest.size(); i++) {
//            for(int j = 0; j < endNearest.size(); j++) {
//                Vertex startStation = startNearest.getObject(i);
//                Vertex endStation = endNearest.getObject(j);
//                for(int k = 0; k < startStation.getLines().size(); k++) {
//                    vertices.add(startNearest.getObject(0));
//                    lines.add(startNearest.getObject(0).getLines().get(i));
//                }
//                initialize(startStation);
//                anotherNoName(vertices, lines, -1);
//                //生成返回值
//                String tempLine = "";
//                while(endStation != startNearest.getObject(0)) {
//                    if(endStation.getPreList().size() > 0) {
//                        for(int n = 0; n < endStation.getPreList().size(); n++) {
//                            if(endStation.getPreList().get(n).getLine().equals(tempLine))
//                                endStation.setPre(endStation.getPreList().get(n));
//                        }
//                    }
//                    System.out.println(endStation.getAddress() + " " + endStation.getPreList().size() + " " + endStation.getWeight());
//                    tempLine = endStation.getPre().getLine();
//                    endStation = endStation.getPre().getStart();
//                }
//            }
//        }
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
    private void anotherNoName(ArrayList<Vertex> vertices, ArrayList<Line> lines, int transfer) {
        while(vertices.size() > 0 && lines.size() > 0) {
            transfer++;
            ArrayList<Vertex> tempVertex = new ArrayList<>(); //储存中转站
            ArrayList<Line> tempLine = new ArrayList<>(); //储存中转站对应的地铁线
            for(int i = 0; i < vertices.size(); i++) {
                Line line = lines.get(i);
                int index = line.getStations().indexOf(vertices.get(i));
                if(!line.isWalk()) {

                    //以中转站为中心向两边推进
                    for(int j = index - 1; j >= 0; j--) {
                        if(line.getStations().get(j).getTransfer() == line.getStations().get(j + 1).getTransfer()) {
                            line.getStations().get(j).getPreList().add(getEdge(line, line.getStations().get(j + 1), line.getStations().get(j)));
                        }
                        if(line.getStations().get(j).getTransfer()  > line.getStations().get(j + 1).getTransfer()) {
                            Edge pre = getEdge(line, line.getStations().get(j + 1), line.getStations().get(j));
                            int temp = j;
                            while(pre == null) {
                                pre = getEdge(line, line.getStations().get(temp), line.getStations().get(j));
                                temp++;
                            }
                            line.getStations().get(j).setPre(pre);
                            //updateWeight(transfer, line, j);
                            line.getStations().get(j).setTransfer(transfer);
                            line.getStations().get(j).setWeight(pre.getStart().getWeight() + pre.getTime());
                            line.getStations().get(j).setTransfer(transfer);
                        }
                    }

                    for(int j = index + 1; j < line.getStations().size(); j++) {
                        if(line.getStations().get(j).getTransfer() == line.getStations().get(j - 1).getTransfer()) {
                            line.getStations().get(j).getPreList().add(getEdge(line, line.getStations().get(j - 1), line.getStations().get(j)));
                        }
                        if(line.getStations().get(j).getTransfer()  > line.getStations().get(j - 1).getTransfer()) {
                            //line.getStations().get(j).setPre(getEdge(line, line.getStations().get(j - 1), line.getStations().get(j)));
                            Edge pre = getEdge(line, line.getStations().get(j - 1), line.getStations().get(j));
                            int temp = j - 1;
                            while(pre == null) {
                                pre = getEdge(line, line.getStations().get(temp), line.getStations().get(j));
                                temp--;
                            }
                            line.getStations().get(j).setPre(pre);
                            line.getStations().get(j).setWeight(pre.getStart().getWeight() + pre.getTime());
                            line.getStations().get(j).setTransfer(transfer);
                            //updateWeight(transfer, line, j);
                        }
                    }
                   // update(vertices.get(i), line);

                    //是否存在需要更多换乘的站点
                    for(int j = 0; j < line.getTransfer().size(); j++) {
                        Vertex vertex = line.getTransfer().get(j);
                        for(int k = 0; k < vertex.getLines().size(); k++) {
                            if(!vertex.getLines().get(k).isWalk()) {
                                tempVertex.add(vertex);
                                tempLine.add(vertex.getLines().get(k));
                            }
                        }
                    }
                }
                line.setWalk(true);
            }
            vertices = tempVertex;
            lines = tempLine;
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
            aVertex.setTransfer(Integer.MAX_VALUE);
            aVertex.setPre(null);
            aVertex.getPreList().clear();
        }
        start.setWeight(0);
        start.setTransfer(0);
    }

    //更新站点权重
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
        Vertex temp = endStation;
        while (endStation != startStation) {
            subwayList.add(0, new Address(endStation.getAddress(),
                    Double.toString(endStation.getCoords()[0]), Double.toString(endStation.getCoords()[1])));
            endStation = endStation.getPre().getStart();
        }
        subwayList.add(0, new Address(startStation.getAddress(),
                Double.toString(startStation.getCoords()[0]), Double.toString(startStation.getCoords()[1])));
        returnValue.setSubwayList(subwayList);
        //设置时间和步行距离
        calculateTime(start, end, startStation, temp, returnValue);
        return returnValue;
    }

    //计算路程所花费的时间
    private void calculateTime(Address start, Address end, Vertex startStation, Vertex endStation, ReturnValue returnValue) {
        double distant1 = startStation.distanceTo(start);
        double distant2 = endStation.distanceTo(end);
        double time = distant1 / SPEED + endStation.getWeight() + distant2 / SPEED;
        returnValue.setWalkDistan(distant1 + distant2);
        returnValue.setMinutes(time);
    }

    private void update(Vertex station, Line line, int transfer) {
        for(int i = 0; i < station.getAdj().size(); i++) {
            if(station.getAdj().get(i).getLine().equals(line.getName()) && station.getAdj().get(i).getEnd().getWeight() >= station.getWeight()) {
                Vertex end = station.getAdj().get(i).getEnd();
                if(end.getTransfer() == station.getTransfer()) {
                    end.getPreList().add(getEdge(line, station, end));
                }
                if(end.getTransfer() > station.getTransfer()) {
                    end.setPre(getEdge(line, station, end));
                    end.setWeight(station.getWeight() + Objects.requireNonNull(getEdge(line, station, end)).getTime());
                    end.setTransfer(transfer);
                }
                update(end, line, transfer);
            }
        }
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
