package com.fudan.sw.dsa.project2.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fudan.sw.dsa.project2.bean.Address;
import com.fudan.sw.dsa.project2.bean.Graph;
import com.fudan.sw.dsa.project2.bean.ReturnValue;
import com.fudan.sw.dsa.project2.constant.FileGetter;

/**
 * this class is what you need to complete
 * @author zjiehang
 *
 */
@Service
public class IndexService
{
	//the subway graph
	private Graph graph = null;

	/**
	 * create the graph use file
	 */
	public void createGraphFromFile() {
		//如果图未初始化
		if(graph==null)
		{
			FileGetter fileGetter= new FileGetter();
			try(BufferedReader bufferedReader=new BufferedReader(new FileReader(fileGetter.readFileFromClasspath())))
			{
				String line = null;
				while((line=bufferedReader.readLine())!=null)
				{
					System.out.println(line);
				}
				//create the graph from file
				graph = new Graph();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}


	public ReturnValue travelRoute(Map<String, Object> params) {
		String startAddress = 	params.get("startAddress").toString();
		String startLongitude = params.get("startLongitude").toString();
		String startLatitude = params.get("startLatitude").toString();
		String endAddress = params.get("endAddress").toString();
		String endLongitude = params.get("endLongitude").toString();
		String endLatitude = params.get("endLatitude").toString();
		String choose = params.get("choose").toString();

		System.out.println(startAddress);
		System.out.println(startLongitude);
		System.out.println(startLatitude);
		System.out.println(endAddress);
		System.out.println(endLongitude);
		System.out.println(endLatitude);
		System.out.println(choose);

		Address startPoint = new Address(startAddress, startLongitude, startLatitude);
		Address endPoint = new Address(endAddress, endLongitude, endLatitude);
		ReturnValue returnValue = null;
		switch (choose) {
			case "1":
				//步行最少
				returnValue = graph.leastWalk(startPoint, endPoint);
				break;
			case "2":
				//换乘最少
				break;
			case "3":
				//时间最短:
				returnValue = graph.shortestTime(startPoint, endPoint);
				break;
			default:
				break;
		}
		return returnValue;
	}
}
