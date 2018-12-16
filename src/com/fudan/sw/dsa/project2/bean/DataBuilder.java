package com.fudan.sw.dsa.project2.bean;

import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DataBuilder {
    public DataBuilder () {

    }

    public void loadMap(File input, ArrayList<Vertex> addressList, MyKdTree<Vertex> stations, ArrayList<Line> lines) {
        try {
            Workbook book = Workbook.getWorkbook(input);
            int sheetSize = book.getNumberOfSheets();
            for(int i = 0; i < sheetSize; i++) {
                //每个sheet 获取几号线
                Sheet sheet = book.getSheet(i);
                String lineName = sheet.getName();
                //创建Line对象
                Line line = new Line(lineName);
                lines.add(line);
                int columns = sheet.getColumns();
                int rows = sheet.getRows();
                //先加入所有的站
                for(int row = columns - 3; row < rows; row++) {
                    Vertex station = exist(addressList, sheet.getCell(0, row).getContents(), ((NumberCell)sheet.getCell(1, row)).getValue(), ((NumberCell)sheet.getCell(2, row)).getValue());
                    //将站点按顺序加入到相应的Line中
                    line.addStation(station);
                }
                //构建站与站之间的连接关系
                for(int column = 3; column < columns; column++) {
                    Vertex newStation = exist(addressList, sheet.getCell(0, columns - 3).getContents(), ((NumberCell)sheet.getCell(1, columns - 3)).getValue(), ((NumberCell)sheet.getCell(2, columns - 3)).getValue());
                    String startTime = sheet.getCell(column, columns - 3).getContents();
                    for(int row = columns - 2; row < rows; row++) {
                        Vertex sucStation = exist(addressList, sheet.getCell(0, row).getContents(), ((NumberCell)sheet.getCell(1, row)).getValue(), ((NumberCell)sheet.getCell(2, row)).getValue());
                        String endTime = sheet.getCell(column, row).getContents();
                        if(endTime.equals("--"))
                            continue;
                        addEdge(newStation, sucStation, line, startTime, endTime);
                        newStation = sucStation;
                        startTime = endTime;
                    }
                }
            }
            for (Vertex address : addressList) {
                //将站点插入到KdTree当中
                stations.add(address.getCoords(), address);
                //向Line中添加换乘点
                int size = address.getLines().size();
                if (size > 1) {
                    for (int j = 0; j < size; j++) {
                        Line line = address.getLines().get(j);
                        line.addTransfer(address);
                    }
                }
            }
        }catch (BiffException | IOException e) {
            e.printStackTrace();
        }
    }

    //判断该站是否已被加入
    public Vertex exist(ArrayList<Vertex> stations, String name, double longitude, double latitude) {
        for(Vertex station : stations) {
            if(station.getAddress().equals(name))
                return station;
        }
        Vertex station = new Vertex(name, longitude, latitude);
        stations.add(station);
        return station;
    }
    //加边
    public void addEdge(Vertex start, Vertex end, Line line, String startTime, String endTime) {
        int cost = getCost(startTime, endTime);
        Edge edge = new Edge( cost, line.getName(), start, end);
        boolean isIn = false;
        for(int i = 0; i < start.getAdj().size(); i++) {
            Edge temp = start.getAdj().get(i);
            if(temp.getStart().getAddress().equals(edge.getStart().getAddress()) &&
                    temp.getEnd().getAddress().equals(edge.getEnd().getAddress()) && temp.getLine().equals(edge.getLine()))
                isIn = true;
        }
        if(!isIn) {
            start.getAdj().add(new Edge( cost, line.getName(), start, end));
            end.getAdj().add(new Edge( cost, line.getName(), end, start));
        }
        if(start.getLines().indexOf(line) < 0)
            start.getLines().add(line);
        if(end.getLines().indexOf(line) < 0)
            end.getLines().add(line);
    }
    //计算时间差
    public int getCost(String start, String end) {
        int length1 = start.length();
        int length2 = end.length();
        int minute = (int)end.charAt(length2 - 1) - (int)start.charAt(length1 - 1);
        int ten = (int)end.charAt(length2 - 2) - (int)start.charAt(length1 - 2);
        int hour = (int)end.charAt(length2 - 4) - (int)start.charAt(length1 - 4);
        hour = hour >= 0? hour : hour + 4;
        return hour * 60 + ten * 10 + minute;
    }
}
