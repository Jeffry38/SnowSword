package com.leixiao.snowsword.model;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name="info")
public class Info {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id", nullable = false)
    private int id;

    @Column(name="os", nullable = false)
    private String os;

    @Column(name="version", nullable = false)
    private String version;

    @Column(name="user", nullable = false)
    private String user;

    @Column(name="ip_address", nullable = false)
    private String ip_address;

    @Column(name="address", nullable = false)
    private String address;

    @Column(name="update_time", nullable = false)
    private Date update_time;

    @Column(name="hostname", nullable = false)
    private String hostname;

    @Column(name="script_filename", nullable = false)
    private String script_filename;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getIp_address() {
        return ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUpdate_time() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(update_time);
    }

    public void setUpdate_time(Date update_time) {
        this.update_time = update_time;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getScript_filename() {
        return script_filename;
    }

    public void setScript_filename(String script_filename) {
        this.script_filename = script_filename;
    }
}
