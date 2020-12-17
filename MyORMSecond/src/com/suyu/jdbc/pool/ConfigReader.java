package com.suyu.jdbc.pool;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

/**
 * 这个类出现的目的是为了在程序启动时预读取配置文件中的信息
 * 将信息存入缓存，方便之后的使用
 * 为什么只写一个configuration配置文件？
 * 因为我们的ORM配置信息都在这一个文件中即可，没必要分mysql的配置文件和oracle的配置文件
 * 后期如果有需求更改这一个配置文件即可，以后ORM打成jar就不能更改代码了，所以只有一个文件就能写死了
 */
public class ConfigReader {

    //静态属性map充当缓存存储配置信息
    private static HashMap<String,String> configMap = new HashMap<>();

    /**
     * 静态块保证只执行一次将配置文件中的信息存入缓存
     */
    static {
        try {
            //通过当前线程获取类加载器，类加载器有自己的方法找到我们对应的那个文件的具体路径，然后组成InputStream流给我们
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("configuration.properties");
            Properties pro = new Properties();
            pro.load(is);
            Enumeration en = pro.propertyNames();
            while(en.hasMoreElements()){
                String key = (String) en.nextElement();
                String value = pro.getProperty(key);
                configMap.put(key,value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getConfigValue(String key){
        return configMap.get(key);
    }
}
