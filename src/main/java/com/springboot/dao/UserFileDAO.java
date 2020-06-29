package com.springboot.dao;

import com.springboot.entity.UserFile;

import java.util.List;

public interface UserFileDAO {
    List<UserFile> findByUserId(Integer id);

    void save(UserFile userFile);

    UserFile findById(Integer id);

    void update(UserFile userFile);

    void delete(Integer id);
}
