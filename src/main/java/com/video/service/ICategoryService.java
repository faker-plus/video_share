package com.video.service;

import com.video.entity.Category;

import java.util.List;

/**
 * @author tian
 * @since 2023/11/30
 */
public interface ICategoryService {

    /**
     * Add a category
     *
     * @param name Classification name
     *
     * @return Successfully added
     */
    boolean insert(String name);

    /**
     * Delete a category
     *
     * @param id Classification number
     *
     * @return Successfully deleted
     */
    boolean remove(int id);

    /**
     * Update classification
     *
     * @param id Classification number
     * @param name Classification name
     *
     * @return Is the update successful
     */
    boolean update(int id, String name);

    /**
     * Obtain a classification
     *
     * @param id Classification number
     *
     * @return {@link Category}
     */
    Category getById(int id);

    /**
     * Obtain all categories
     *
     * @return {@link List}
     */
    List<Category> list();

    /**
     * Obtain ID through classification name
     *
     * @param name Classification name
     *
     * @return {@link Integer}
     */
    int getIdByName(String name);
}
