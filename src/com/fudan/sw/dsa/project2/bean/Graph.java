package com.fudan.sw.dsa.project2.bean;

import com.fudan.sw.dsa.project2.constant.FileGetter;
//import jdk.nashorn.internal.runtime.arrays.ArrayLikeIterator;
//import sun.jvm.hotspot.debugger.AddressException;
//import com.sun.xml.internal.ws.api.server.DocumentAddressResolver;
//import com.sun.xml.internal.ws.api.server.DocumentAddressResolver;
//import sun.jvm.hotspot.runtime.VMReg;
//import javafx.geometry.VerticalDirection;
//import sun.jvm.hotspot.opto.MachIfNode;
//import jdk.internal.module.SystemModuleFinders;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * for subway graph
 * @author dengpeng
 *
 */
public class Graph {
    private static final int DIMENSION = 2;  //KdTree维度
    private static final int SPEED = 83;  //步行速度
    private static final int NUM = 5;
    private ArrayList<Vertex> stationsList;
    private MyKdTree<Vertex> stations;
    private ArrayList<Line> lines;
    public Graph() {
        this.stations = new MyKdTree<>(DIMENSION);
        this.stationsList = new ArrayList<>();
        this.lines = new ArrayList<>();
        makeGraph();
    }

    private void makeGraph() {
        FileGetter fileGetter = new FileGetter();
        File file = fileGetter.readFileFromClasspath();
        DataBuilder dataBuilder = new DataBuilder();
        dataBuilder.loadMap(file, stationsList, stations, lines);
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
                System.out.println(startNearest.getObject(i).getAddress() + endNearest.getObject(j).getAddress() + "时间： " + temp.getMinutes());
                if(temp.getMinutes() < returnValue.getMinutes())
                    returnValue = temp;
            }
        }
        //System.out.println(returnValue.getMinutes());
        return returnValue;
    }

    public ReturnValue newShortestTime(Address start, Address end) {
        ArrayList<Vertex> startNearest = getNearests(start);
        ArrayList<Vertex> endNearest = getNearests(end);
        ReturnValue returnValue = new ReturnValue();
        returnValue.setMinutes(Double.MAX_VALUE);
        for(int i = 0; i < startNearest.size(); i++) {
            for(int j = 0; j < endNearest.size(); j++) {
                Dijkstra(startNearest.get(i), endNearest.get(j));
                ReturnValue temp = getReturn(start, end, startNearest.get(i), endNearest.get(j));
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

    public ReturnValue newLeastWalk(Address start, Address end) {
        Dijkstra(getNearests(start).get(0), getNearests(end).get(0));
        return  getReturn(start, end, getNearests(start).get(0), getNearests(end).get(0));
    }

    //Find the least transfer path
    public ReturnValue leastTransfer(Address start, Address end) {
        ResultHeap<Vertex> startNearest = stations.getNearestNeighbors(new double[] {start.getLongitude(), start.getLatitude()}, NUM);
        ResultHeap<Vertex> endNearest = stations.getNearestNeighbors(new double[] {end.getLongitude(), end.getLatitude()}, NUM);
        ReturnValue returnValue = new ReturnValue();
        int leastTransfer = Integer.MAX_VALUE;
        for(int i = 0; i < startNearest.size(); i++) {
            for(int j = 0; j < endNearest.size(); j++) {
                ArrayList<Vertex> vertices = new ArrayList<>();
                ArrayList<Line> lines = new ArrayList<>();
                Vertex startStation = startNearest.getObject(i);
                Vertex endStation = endNearest.getObject(j);
                for(int k = 0; k < startStation.getLines().size(); k++) {
                    vertices.add(startStation);
                    lines.add(startStation.getLines().get(k));
                }
                initialize(startStation);
                newNoName(vertices, lines, -1);
                if(endStation.getTransfer() <= leastTransfer) {
                    ReturnValue tempReturn = new ReturnValue();
                    //生成返回值
                    tempReturn.setStartPoint(start);
                    tempReturn.setEndPoint(end);
                    List<Address> subwayList = new ArrayList<>();
                    String tempLine = "";
                    Vertex temp = endStation;
                    while(endStation != startStation) {
                        if(endStation.getPreList().size() > 0) {
                            for(int n = 0; n < endStation.getPreList().size(); n++) {
                                if(endStation.getPreList().get(n).getLine().equals(tempLine))
                                    endStation.setPre(endStation.getPreList().get(n));
                            }
                        }
                        subwayList.add(0, new Address(endStation.getAddress(),
                                Double.toString(endStation.getCoords()[0]), Double.toString(endStation.getCoords()[1])));
                        tempLine = endStation.getPre().getLine();
                        endStation = endStation.getPre().getStart();
                    }
                    subwayList.add(0, new Address(startStation.getAddress(),
                            Double.toString(startStation.getCoords()[0]), Double.toString(startStation.getCoords()[1])));
                    tempReturn.setSubwayList(subwayList);
                    //设置时间和步行距离
                    calculateTime(start, end, startStation, temp, tempReturn);
                    if(temp.getTransfer() == leastTransfer && tempReturn.getMinutes() < returnValue.getMinutes())
                        returnValue = tempReturn;
                    else if(temp.getTransfer() < leastTransfer) {
                        returnValue = tempReturn;
                        leastTransfer = temp.getTransfer();
                    }
                }
            }
        }
        return returnValue;
    }

    public ReturnValue newLeastTransfer(Address start, Address end) {
        ArrayList<Vertex> startNearest = getNearests(start);
        ArrayList<Vertex> endNearest = getNearests(end);
        ReturnValue returnValue = new ReturnValue();
        int leastTransfer = Integer.MAX_VALUE;
        for(int i = 0; i < startNearest.size(); i++) {
            for(int j = 0; j < endNearest.size(); j++) {
                ArrayList<Vertex> vertices = new ArrayList<>();
                ArrayList<Line> lines = new ArrayList<>();
                Vertex startStation = startNearest.get(i);
                Vertex endStation = endNearest.get(j);
                for(int k = 0; k < startStation.getLines().size(); k++) {
                    vertices.add(startStation);
                    lines.add(startStation.getLines().get(k));
                }
                initialize(startStation);
                newNoName(vertices, lines, -1);
                if(endStation.getTransfer() <= leastTransfer) {
                    ReturnValue tempReturn = new ReturnValue();
                    //生成返回值
                    tempReturn.setStartPoint(start);
                    tempReturn.setEndPoint(end);
                    List<Address> subwayList = new ArrayList<>();
                    String tempLine = "";
                    Vertex temp = endStation;
                    while(endStation != startStation) {
                        if(endStation.getPreList().size() > 0) {
                            for(int n = 0; n < endStation.getPreList().size(); n++) {
                                if(endStation.getPreList().get(n).getLine().equals(tempLine))
                                    endStation.setPre(endStation.getPreList().get(n));
                                else {
                                    for(int l = 0; l < startStation.getLines().size(); l++) {
                                        if(endStation.getPreList().get(n).getLine().equals(startStation.getLines().get(l).getName()))
                                            endStation.setPre(endStation.getPreList().get(n));
                                    }
                                }
                            }
                        }
                        subwayList.add(0, new Address(endStation.getAddress(),
                                Double.toString(endStation.getCoords()[0]), Double.toString(endStation.getCoords()[1])));
                        tempLine = endStation.getPre().getLine();
                        endStation = endStation.getPre().getStart();
                    }
                    subwayList.add(0, new Address(startStation.getAddress(),
                            Double.toString(startStation.getCoords()[0]), Double.toString(startStation.getCoords()[1])));
                    tempReturn.setSubwayList(subwayList);
                    //设置时间和步行距离
                    calculateTime(start, end, startStation, temp, tempReturn);
                    if(temp.getTransfer() == leastTransfer && tempReturn.getMinutes() < returnValue.getMinutes())
                        returnValue = tempReturn;
                    else if(temp.getTransfer() < leastTransfer) {
                        returnValue = tempReturn;
                        leastTransfer = temp.getTransfer();
                    }
                }
            }
        }
        return returnValue;
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
                if(line.isWalk()) {

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
                            if(vertex.getLines().get(k).isWalk()) {
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

    private void newNoName(ArrayList<Vertex> vertices, ArrayList<Line> lines, int transfer) {
        while(vertices.size() > 0 && lines.size() > 0) {
            transfer++;
            ArrayList<Vertex> tempVertex = new ArrayList<>(); //储存中转站
            ArrayList<Line> tempLine = new ArrayList<>(); //储存中转站对应的地铁线
            for(int i = 0; i < vertices.size(); i++) {
                Line line = lines.get(i);
                if(!line.isWalk()) {
                    line.setWalk(true);
                     update(vertices.get(i), line, transfer);
                    //是否存在需要更多换乘的站点
                    for(int j = 0; j < line.getTransfer().size(); j++) {
                        Vertex vertex = line.getTransfer().get(j);
                        for(int k = 0; k < vertex.getLines().size(); k++) {
                            Line temp = vertex.getLines().get(k);
                            if(lines.indexOf(vertex.getLines().get(k)) < 0 && !vertex.getLines().get(k).isWalk()) {
                                tempVertex.add(vertex);
                                tempLine.add(vertex.getLines().get(k));
                            }
                        }
                    }
                }
            }
            vertices.clear();
            vertices.addAll(tempVertex);
            tempVertex.clear();
            lines.clear();
            lines.addAll(tempLine);
            tempLine.clear();
        }
    }
    private Edge getEdge(Line line, Vertex start, Vertex end) {
        for(int j = 0; j < start.getAdj().size(); j++)
            if(start.getAdj().get(j).getLine().equals(line.getName()) && start.getAdj().get(j).getEnd() == end)
                return start.getAdj().get(j);
        return null;
    }

    //初始化站点的权重
    private void initialize(Vertex start) {
        for(Vertex aVertex : stationsList) {
            aVertex.setWeight(Integer.MAX_VALUE);
            aVertex.setTransfer(Integer.MAX_VALUE);
            aVertex.setPre(null);
            aVertex.getPreList().clear();
        }
        for(Line line : lines) {
            line.setWalk(false);
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

    private ArrayList<Vertex> getNearests(Address address) {
        for (Vertex vertex : stationsList) {
            vertex.setDistance(vertex.distanceTo(address));
        }
        //插入排序
        ArrayList<Vertex> temp = new ArrayList<>(stationsList);
        for(int i = 0; i < temp.size(); i++) {
            Vertex vertex = temp.get(i);
            int j = i - 1;
            while((j >= 0) && (temp.get(j).getDistance() > vertex.getDistance())) {
                temp.set((j + 1), temp.get(j));
                j--;
            }
            temp.set(j + 1, vertex);
        }
        ArrayList<Vertex> result = new ArrayList<>();
        for(int i = 0; i <= 5; i ++) {
            result.add(temp.get(i));
        }
        return result;
    }

    private void update(Vertex station, Line line, int transfer) {
        for(int i = 0; i < station.getAdj().size(); i++) {
            Edge edge = station.getAdj().get(i);
            Vertex end = edge.getEnd();
            if (edge.getLine().equals(line.getName())){
                if (end.getTransfer() == Integer.MAX_VALUE){
                    end.setPre(edge);
                    end.setWeight(station.getWeight() + edge.getTime());
                    end.setTransfer(transfer);
                    update(end,line,transfer);
                }
                else if (station.getPre() != null  && station.getAdj().get(i).getEnd() != station.getPre().getStart()){
                    boolean flag = true;
                    for (Edge e:end.getPreList()){
                        if (e.getLine().equals(line.getName())){
                            flag = false;
                        }
                    }
                    if(end.getPre().getLine().equals(line.getName()))
                        flag = false;
                    if (flag){
                        end.getPreList().add(end.getPre());
                        end.setPre(edge);
                        update(end,line,transfer);
                    }
                }
            }
//            if(station.getAdj().get(i).getLine().equals(line.getName()) && (station.getPre() == null  || station.getAdj().get(i).getEnd() != station.getPre().getStart())) {
//                Vertex end = station.getAdj().get(i).getEnd();
//                Edge edge = getEdge(line, station, end);
//                if(edge != null) {
//                    if(end.getTransfer() == station.getTransfer()) {
//                        end.getPreList().add(end.getPre());
//                        end.setPre(edge);
//                    }else if (end.getTransfer() > station.getTransfer()) {
//                        end.setPre(edge);
//                        end.setWeight(station.getWeight() + edge.getTime());
//                        end.setTransfer(transfer);
//                    }
//                }
//                update(end, line, transfer);
//            }
        }
    }
}
