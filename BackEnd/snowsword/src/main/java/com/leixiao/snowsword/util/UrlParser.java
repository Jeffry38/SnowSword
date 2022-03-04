package com.leixiao.snowsword.util;

import org.apache.commons.io.FileUtils;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;

public class UrlParser {
    private String url;
    private String ip;
    private String address;

    public UrlParser(String url){   //构造函数，传入url，接着调用两个函数分别获取url的ip和ip的物理地址
        this.url = url;
        this.ip = url2IP(this.url);
        this.address = IP2Address(this.ip);
    }

    private String url2IP(String url){  //通过url获取ip地址
        String ip;
        try {
            String hostName = new URL(url).getHost();
            ip = InetAddress.getByName(hostName).getHostAddress();
        }catch (Exception exception){
            ip =  exception.toString();
        }
        return ip;
    }

    private String IP2Address(String ip){   //通过ip地址获取物理地址
        String address;
        try {
            String tmpDir = System.getProperties().getProperty("java.io.tmpdir");
            String dbPath = tmpDir + File.separator + "ip2region.db";
            File file = new File(dbPath);
            if (file.exists() == false) {
                FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("/ip2region.db"), file);
            }
            DbConfig config = new DbConfig();
            DbSearcher searcher = new DbSearcher(config, dbPath);
            DataBlock block = searcher.btreeSearch(ip);
            address = block.getRegion();
        }catch (Exception exception){
            address =  exception.toString();
        }
        return address;
    }

    public String getIp() {
        return this.ip;
    }

    public String getAddress() {
        return this.address;
    }
}
