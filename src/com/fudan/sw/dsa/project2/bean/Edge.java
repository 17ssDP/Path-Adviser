package com.fudan.sw.dsa.project2.bean;

//import org.graalvm.compiler.core.common.type.ArithmeticOpTable;
//import sun.jvm.hotspot.debugger.AddressException;

public class Edge {
    private int time;
    private String line;
    private Vertex start;
    private Vertex end;
    public Edge(int time, String line, Vertex start, Vertex end) {
        this.time = time;
        this.line = line;
        this.start = start;
        this.end = end;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public Vertex getStart() {
        return start;
    }

    public void setStart(Vertex start) {
        this.start = start;
    }

    public Vertex getEnd() {
        return end;
    }

    public void setEnd(Vertex end) {
        this.end = end;
    }
}
