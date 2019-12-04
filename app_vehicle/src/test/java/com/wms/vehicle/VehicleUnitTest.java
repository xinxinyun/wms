package com.wms.vehicle;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * @author 周宇
 * 版本：1.0
 * 创建日期：${date}
 * 描述：
 */
public class VehicleUnitTest {

    @Test
    public void splitStr(){
        String path = "D:/a.txt";
        Scanner in = null;
        try {
            in = new Scanner(new File(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //List data = new ArrayList();
        while (in.hasNextLine()) {
            // 取第一行
            String s = in.nextLine();
            char[] sstr = s.toCharArray();
            String afterStr = "";
            for (int i = 0; i < sstr.length; i++) {
                afterStr += sstr[i];
                if (i % 2 == 1 && i != 0) {
                    afterStr += " ";
                }
            }
            System.out.println("------------>" + afterStr);
            //System.out.println(ASCUtil.str12to17(afterStr));
        }
    }
}
