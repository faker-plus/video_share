package com.video.dao;

import com.video.entity.Category;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author tian
 * @since 2023/11/18
 */
@Repository
public interface CategoryDAO {

    /**
     * Obtain ID through classification name
     *
     * @param name Classification name
     *
     * @return {@link Integer}
     */
    @Select("select id from category where name=#{name}")
    int getIdByName(String name);

    /**
     * Add a category
     *
     * @param name Classification name
     *
     * @return Successfully added
     */
    @Insert("insert into category(name) values(#{name})")
    boolean insertCategory(String name);

    /**
     * Delete a classification by numbering
     *
     * @param id number
     *
     * @return Successfully deleted
     */
    @Delete("delete from category where id=#{id}")
    boolean removeCategoryById(int id);

    /**
     * Delete a classification by name
     *
     * @param name Classification name
     *
     * @return Successfully deleted
     */
    @Delete("delete from category where name=#{name}")
    boolean removeCategoryByName(String name);

    /**
     * Update a category name
     *
     * @param name Classification name
     * @param id Classification ID
     *
     * @return Is the update successful
     */
    @Update("update category set name=#{name} where id=#{id}")
    boolean updateNameById(@Param("id") int id, @Param("name") String name);

    /**
     * Update category names through category names
     *
     * @param newName New classification name
     * @param oldName Old classification name
     */
    @Update("update category set name=#{newName} where name=#{oldName}")
    void updateNameByName(String newName, String oldName);

    /**
     * Get all categories
     *
     * @return {@link List}
     */
    @Select("select * from category")
    List<Category> listCategory();

    /**
     * Obtain a classification by numbering
     *
     * @param id NUMBER
     *
     * @return {@link Category}
     */
    @Select("select * from category where id=#{id}")
    Category getCategoryById(int id);

    /**
     * Obtain a classification by name
     *
     * @param name NAME
     *
     * @return {@link Category}
     */
    @Select("select * from category where name=#{name}")
    Category getCategoryByName(String name);
}
