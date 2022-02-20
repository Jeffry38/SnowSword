package com.leixiao.snowsword.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.net.InetAddress;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private String IP2Address(String ip){   //通过ip地址获取物理地址，具体方法是通过请求www.ip138.com网站，从返回包里提取数据
        System.out.println(111);
        String address;
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(2, TimeUnit.SECONDS)
                    .readTimeout(3,TimeUnit.SECONDS)
                    .build();
            Request request = new Request
                    .Builder()
                    .url("https://www.ip.cn/ip/"+ip+".html")
                    .header("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.55 Safari/537.36")
                    .build();
            Response response = client.newCall(request).execute();
            String responseBody = new String(response.body().bytes(), "utf-8");
            Matcher matcher = Pattern.compile("<div id=\"tab0_address\">(.*?)</div>").matcher(responseBody);
            if(matcher.find()){
                address = matcher.group(1).trim();
            }else{
                address = "UNKNOWN";
            }
        }catch (Exception exception){
            address =  exception.toString();
        }
        System.out.println(222);
        return address;
    }

    public String getIp() {
        return this.ip;
    }

    public String getAddress() {
        return this.address;
    }
}
