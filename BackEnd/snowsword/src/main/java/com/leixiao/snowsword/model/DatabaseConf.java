package com.leixiao.snowsword.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name="database_conf")
public class DatabaseConf {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id", nullable = false)
    private int id;

    @JsonIgnore
    @JSONField(serialize = false)
    @ManyToOne(targetEntity = Webshell.class,fetch = FetchType.LAZY)
    @JoinColumn(name = "webshell_id",referencedColumnName = "id")
    private Webshell webshell;

    @Column(name="host", nullable = false)
    private String host;

    @Column(name="port", nullable = false)
    private Integer port;

    @Column(name="username", nullable = false)
    private String username;

    @Column(name="password", nullable = false)
    private String password;

    @Column(name="code", nullable = false)
    private String code;

    @Column(name="update_time", nullable = false)
    private Date update_time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Webshell getWebshell() {
        return webshell;
    }

    public void setWebshell(Webshell webshell) {
        this.webshell = webshell;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUpdate_time() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(update_time);
    }

    public void setUpdate_time(Date update_time) {
        this.update_time = update_time;
    }
}
