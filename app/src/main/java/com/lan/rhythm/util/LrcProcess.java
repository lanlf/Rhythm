package com.lan.rhythm.util;

import android.view.animation.AnimationUtils;

import com.lan.rhythm.model.Lrc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *处理歌词的类
 *  Created by lan on 2016/7/21.
 */
public class LrcProcess {
    private List<Lrc> lrcList; //List集合存放歌词内容对象
    private Lrc mLrcContent;     //声明一个歌词内容对象
    /**
     * 无参构造函数用来实例化对象
     */
    public LrcProcess() {
        mLrcContent = new Lrc();
        lrcList = new ArrayList<Lrc>();
    }

    /**
     * 读取歌词
     * @param path
     * @return
     */
    public String readLRC(String path) {
        //定义一个StringBuilder对象，用来存放歌词内容
        StringBuilder stringBuilder = new StringBuilder();
        File f = new File(path.replace(".mp3", ".lrc"));
        try {
            //创建一个文件输入流对象
            FileInputStream fis = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(fis, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String s = "";
            while((s = br.readLine()) != null) {
                //替换字符
                s = s.replace("[", "");
                s = s.replace("]", "@");

                //分离“@”字符
                String splitLrcData[] = s.split("@");
                if(splitLrcData.length > 1) {
                    mLrcContent.setLrcStr(splitLrcData[1]);

                    //处理歌词取得歌曲的时间
                    int lrcTime = time2Str(splitLrcData[0]);

                    mLrcContent.setLrcTime(lrcTime);

                    //添加进列表数组
                    lrcList.add(mLrcContent);
                    System.out.println(mLrcContent.getLrcStr());
                    //新创建歌词内容对象
                    mLrcContent = new Lrc();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            stringBuilder.append("木有歌词文件，赶紧去下载！...");
        } catch (IOException e) {
            e.printStackTrace();
            stringBuilder.append("木有读取到歌词哦！");
        }
        System.out.println(stringBuilder);
        return stringBuilder.toString();
    }
    /**
     * 解析歌词时间
     * 歌词内容格式如下：
     * [00:02.32]陈奕迅
     * [00:03.43]好久不见
     * [00:05.22]歌词制作  王涛
     * @param timeStr
     * @return
     */
    public int time2Str(String timeStr) {
        timeStr = timeStr.replace(":", ".");
        timeStr = timeStr.replace(".", "@");

        String timeData[] = timeStr.split("@"); //将时间分隔成字符串数组

        //分离出分、秒并转换为整型

        int minute;
        int second = 0;
        int millisecond;
        System.out.println(timeData[0]);
        System.out.println(timeData[1]);
        System.out.println(timeData[2]);
        if(timeData[0].equals("00")){
            minute = 0;
        }else if (timeData[0].equals("01")){
            minute = 1;
        } else if (timeData[0].equals("02")){
            minute = 2;
        } else if (timeData[0].equals("03")){
            minute = 3;
        } else if (timeData[0].equals("04")){
            minute = 4;
        } else if (timeData[0].equals("05")){
            minute = 5;
        } else if (timeData[0].equals("06")){
            minute = 6;
        } else if (timeData[0].equals("07")){
            minute = 7;
        } else if (timeData[0].equals("08")){
            minute = 8;
        } else if (timeData[0].equals("09")){
            minute = 9;
        }  else {
            minute = Integer.parseInt(timeData[0]);
        }if(timeData[1].equals("00")){
            second = 0;
        }else if (timeData[1].equals("01")){
            second = 1;
        } else if (timeData[1].equals("02")){
            second = 2;
        } else if (timeData[1].equals("03")){
            second = 3;
        } else if (timeData[1].equals("04")){
            second = 4;
        } else if (timeData[1].equals("05")){
            second = 5;
        } else if (timeData[1].equals("06")){
            second = 6;
        } else if (timeData[1].equals("07")){
            second = 7;
        } else if (timeData[1].equals("08")){
            second = 8;
        } else if (timeData[1].equals("09")){
            second = 9;
        }  else {
            minute = Integer.parseInt(timeData[0]);
        }if(timeData[2].equals("00")){
            millisecond = 0;
        }else if (timeData[2].equals("01")){
            millisecond = 1;
        } else if (timeData[2].equals("02")){
            millisecond = 2;
        } else if (timeData[2].equals("03")){
            millisecond = 3;
        } else if (timeData[2].equals("04")){
            millisecond = 4;
        } else if (timeData[2].equals("05")){
            millisecond = 5;
        } else if (timeData[2].equals("06")){
            millisecond = 6;
        } else if (timeData[2].equals("07")){
            millisecond = 7;
        } else if (timeData[2].equals("08")){
            millisecond = 8;
        } else if (timeData[2].equals("09")){
            millisecond = 9;
        }  else {
            millisecond = Integer.parseInt(timeData[1]);
        }


        //计算上一行与下一行的时间转换为毫秒数
        int currentTime = (minute * 60 + second) * 1000 + millisecond * 10;
        return currentTime;
    }
    public List<Lrc> getLrcList() {
        return lrcList;
    }
}
