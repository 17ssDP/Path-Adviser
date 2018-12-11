package com.fudan.sw.dsa.project2.constant;

import com.fudan.sw.dsa.project2.bean.Address;
import com.fudan.sw.dsa.project2.bean.Edge;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * for constant value
 * @author zjiehang
 *
 */
public class FileGetter
{
	private static final String FILE_PATH = "C:\\Users\\Peng Deng\\Desktop\\Project2\\src\\com\\fudan\\sw\\dsa\\project2\\resource\\subway.xls";

    public File readFileFromClasspath()
    {
    	ClassLoader classLoader = getClass().getClassLoader();
//    	File file = new File(classLoader.getResource(FILE_PATH).getFile());
        File file = new File(FILE_PATH);
        return file;
    }
}
