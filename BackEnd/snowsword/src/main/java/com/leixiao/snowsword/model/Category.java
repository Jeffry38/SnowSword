package com.leixiao.snowsword.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="category")
public class Category {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id", nullable = false)
    private int id;

    @Column(name="name", nullable = false)
    private String name;

    @JsonIgnore
    @JSONField(serialize = false)
    @ManyToMany(mappedBy = "categories")
    private Set<Webshell> webshells = new HashSet<>();

    @Transient  //表示该属性并非一个到数据库表的字段的映射,ORM框架将忽略该属性.
    private int webshellCount;

    public int getWebshellCount() {
        return (int) webshells.stream().count();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Webshell> getWebshells() {
        return webshells;
    }

    public void setWebshells(Set<Webshell> webshells) {
        this.webshells = webshells;
    }

    public void addWebshell(Webshell webshell) {
        Boolean exist = false;
        for(Webshell tmp:this.webshells){
            if(tmp.getId() == webshell.getId()){
                exist = true;
                break;
            }
        }
        if(!exist){
            this.webshells.add(webshell);
        }
    }

    public void removeWebshell(Webshell webshell) {
        for(Webshell tmp:this.webshells){
            if(tmp.getId() == webshell.getId()){
                this.webshells.remove(tmp);
                break;
            }
        }
    }

    public void clearWebshell() {
        for(Webshell tmp:this.webshells){
            tmp.removeCategory(this);
        }
        this.webshells.clear();
    }
}
