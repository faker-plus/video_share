package com.video.dao;

import com.video.dao.sqlprovider.AuthSqlProvider;
import com.video.entity.Auth;
import com.video.model.AuthRecord;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author tian
 * @since 2023/11/19
 */
@Repository
public interface AuthDAO {

    /**
     * Detect if a permission exists
     *
     * @param userId User ID
     * @param fileId Document number
     *
     * @return {@link Auth}
     */
    @Select("select * from auth where user_id=#{userId} and file_id=#{fileId}")
    Auth exists(@Param("userId") int userId, @Param("fileId") long fileId);

    /**
     * Batch Delete Permission Records
     *
     * @param ids Permission Number Set
     *
     * @return Successfully deleted
     */
    @DeleteProvider(type = AuthSqlProvider.class, method = "batchDelete")
    boolean batchDelete(@Param("ids") String ids);

    /**
     * Add a permission record
     *
     * @param auth {@link Auth}
     *
     * @return Successfully added
     */
    @Insert("insert into auth(user_id,file_id,is_downloadable,is_uploadable,is_deletable,is_updatable,is_visible) " +
            "values(#{userId},#{fileId},#{isDownloadable},#{isUploadable},#{isDeletable},#{isUpdatable},#{isVisible})")
    boolean insertAuth(Auth auth);

    /**
     * 删除一条权限记录
     *
     * @param id 编号
     */
    @Delete("delete from auth where id=#{id}")
    void removeAuthById(int id);

    /**
     * 删除一条权限记录
     *
     * @param userId 编号
     */
    @Delete("delete from auth where user_id=#{userId}")
    void removeAuthByUserId(int userId);

    /**
     * 删除一条权限记录
     *
     * @param fileId 编号
     *
     * @return 是否删除成功
     */
    @Delete("delete from auth where file_id=#{fileId}")
    boolean removeAuthByFileId(long fileId);

    /**
     * 更新权限记录
     *
     * @param id 编号
     * @param isDownloadable 下载权限
     * @param isUploadable 上传权限
     * @param isVisible 可查权限
     * @param isDeletable 删除权限
     * @param isUpdatable 更新权限
     *
     * @return 是否更新成功
     */
    @UpdateProvider(type = AuthSqlProvider.class, method = "updateAuthById")
    boolean updateAuthById(@Param("id") long id, @Param("isDownloadable") int isDownloadable, @Param("isUploadable")
            int isUploadable, @Param("isDeletable") int isDeletable, @Param("isUpdatable") int isUpdatable, @Param
            ("isVisible") int isVisible);

    /**
     * 获取权限记录
     *
     * @param id 编号，值小于等于0时不作为条件
     * @param userId 用户编号，值小于等于0时不作为条件
     * @param fileId 文件编号，值小于等于0时不作为条件
     * @param fileName 模糊搜索文件名（当参数不为空时）
     * @param offset 偏移
     *
     * @return {@link List}
     */
    @SelectProvider(type = AuthSqlProvider.class, method = "getAuthBy")
    List<AuthRecord> listAuthBy(@Param("id") long id, @Param("userId") int userId, @Param("fileId") long fileId,
                                @Param("fileName") String fileName, @Param("offset") int offset);
}
