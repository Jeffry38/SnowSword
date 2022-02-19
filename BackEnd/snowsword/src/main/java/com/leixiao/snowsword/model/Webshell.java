package com.leixiao.snowsword.model;


import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="webshell")
public class Webshell {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id", nullable = false)
    private int id;

    @JsonIgnore
    @JSONField(serialize = false)   //若不使用该注解，通过Spring作为Json返回页面时，会出现无限循环的错误
    @ManyToMany(targetEntity = Category.class,cascade = CascadeType.REFRESH)
    @JoinTable(name = "webshell_category",
            //joinColumns,当前对象在中间表中的外键
            joinColumns = {@JoinColumn(name = "webshell_id",referencedColumnName = "id")},
            //inverseJoinColumns，对方对象在中间表的外键
            inverseJoinColumns = {@JoinColumn(name = "category_id",referencedColumnName = "id")}
    )
    private Set<Category> categories = new HashSet<>();

    @OneToOne(cascade=CascadeType.ALL)//webshell是关系的维护端，当删除 webshell，会级联删除 info
    @JoinColumn(name = "info_id", referencedColumnName = "id")//webshell中的info_id字段参考info表中的id字段
    private Info info;

    @JsonIgnore
    @JSONField(serialize = false)
    @OneToMany(targetEntity = DatabaseConf.class, cascade=CascadeType.ALL)
    @JoinColumn(name = "webshell_id",referencedColumnName = "id")
    private Set<DatabaseConf> databases = new HashSet<>();


    @Column(name="url", nullable = false)
    private String url;

    @Column(name="password", nullable = false)
    private String password;

    @Column(name="type", nullable = false)
    private String type;

    @Column(name="note", nullable = false)
    private String note;

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

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public Set<DatabaseConf> getDatabases() {
        return databases;
    }

    public void addDatabase(DatabaseConf databaseConf) {
        Boolean exist = false;
        for(DatabaseConf tmp:this.databases){
            if(tmp.getId() == databaseConf.getId()){
                exist = true;
                break;
            }
        }
        if(!exist){
            this.databases.add(databaseConf);
        }
    }

    public void removeDatabase(DatabaseConf databaseConf) {
        for(DatabaseConf tmp:this.databases){
            if(tmp.getId() == databaseConf.getId()){
                this.databases.remove(tmp);
                break;
            }
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    public void addCategory(Category category) {
        Boolean exist = false;
        for(Category tmp:this.categories){
            if(tmp.getId() == category.getId()){
                exist = true;
                break;
            }
        }
        if(!exist){
            this.categories.add(category);
        }
    }

    public void removeCategory(Category category) {
        for(Category tmp:this.categories){
            if(tmp.getId() == category.getId()){
                this.categories.remove(tmp);
                break;
            }
        }
    }
}
