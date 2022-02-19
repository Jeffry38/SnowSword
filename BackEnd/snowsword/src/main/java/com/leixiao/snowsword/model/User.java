package com.leixiao.snowsword.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="user")
public class User {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id", nullable = false)
    private int id;

    @JsonIgnore
    @JSONField(serialize = false)
    @OneToMany(targetEntity = LoginLog.class)
    @JoinColumn(name = "user_id",referencedColumnName = "id")
    private Set<LoginLog> loginLogs = new HashSet<>();

    @JsonIgnore
    @JSONField(serialize = false)
    @OneToMany(targetEntity = OperationLog.class)
    @JoinColumn(name = "user_id",referencedColumnName = "id")
    private Set<OperationLog> operationLogs = new HashSet<>();

    @Column(name="username", nullable = false)
    private String username;

    @Column(name="password", nullable = false)
    private String password;

    @Column(name="create_time", nullable = false)
    private Date create_time;

    @Column(name="update_time", nullable = false)
    private Date update_time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<LoginLog> getLoginLogs() {
        return loginLogs;
    }

    public void setLoginLogs(Set<LoginLog> loginLogs) {
        this.loginLogs = loginLogs;
    }

    public Set<OperationLog> getOperationLogs() {
        return operationLogs;
    }

    public void setOperationLogs(Set<OperationLog> operationLogs) {
        this.operationLogs = operationLogs;
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

    public String getCreate_time() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(create_time);
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    public String getUpdate_time() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(update_time);
    }

    public void setUpdate_time(Date update_time) {
        this.update_time = update_time;
    }
}
