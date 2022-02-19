package com.leixiao.snowsword.dao;

import com.leixiao.snowsword.model.DatabaseConf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DatabaseConfRepository extends JpaRepository<DatabaseConf, Integer> {
    public List<DatabaseConf> findDatabaseConfsByWebshell_Id(int id);
}
