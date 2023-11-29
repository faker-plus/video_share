package com.video.dao;

import com.video.dao.sqlprovider.UserSqlProvider;
import com.video.entity.User;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author tian
 * @since 2023/11/12
 */
@Repository
public interface UserDAO {

    /**
     * Update user permissions
     *
     * @param id User ID
     * @param permission Authority
     *
     * @return Is the update successful
     */
    @Update("update user set permission=#{permission} where id=#{id}")
    boolean updatePermission(@Param("id") int id, @Param("permission") int permission);

    /**
     * Obtaining User Id with User Name
     *
     * @param usernameOrEmail User name or email
     *
     * @return User ID
     */
    @Select("select id from user where username=#{usernameOrEmail} or email=#{usernameOrEmail}")
    int getUserId(String usernameOrEmail);

    /**
     * Update user basic information through ID
     *
     * @param id Number
     * @param avatar Avatar
     * @param realName RealName
     * @param email Mailbox
     *
     * @return Is the update successful
     */
    @Update("update user set avatar=#{avatar},real_name=#{realName},email=#{email} where id=#{id}")
    boolean updateBasicInfo(@Param("id") int id, @Param("avatar") String avatar, @Param("realName") String realName,
                            @Param("email") String email);

    /**
     * Obtain a user through ID
     *
     * @param id Number
     *
     * @return {@link User}
     */
    @Select("select * from user where id=#{id}")
    User getUserById(int id);

    /**
     * Obtain users through permissions
     *
     * @param permission Authority
     * @param condition Condition
     * @param offset Deviation
     *
     * @return {@link List}
     */
    @SelectProvider(type = UserSqlProvider.class, method = "getUserBy")
    List<User> listUserBy(@Param("permission") int permission, @Param("condition") String condition,
                          @Param("offset") int offset);

    /**
     * User login
     *
     * @param usernameOrEmail USER NAME
     * @param password Password
     *
     * @return {@link User}
     */
    @Select("select * from user where (username=#{usernameOrEmail} or email=#{usernameOrEmail}) and password=sha2" +
            "(#{password},256)")
    User login(@Param("usernameOrEmail") String usernameOrEmail, @Param("password") String password);

    /**
     * Add a user
     *
     * @param user {@link User}
     *
     * @return Successfully inserted
     */
    @Insert("insert into user(username,real_name,email,password,is_downloadable,is_uploadable,is_deletable," +
            "is_updatable,is_visible) values(#{username},#{realName},#{email},sha2(#{password},256)," +
            "#{isDownloadable},#{isUploadable},#{isDeletable},#{isUpdatable},#{isVisible})")
    boolean insertUser(User user);

    /**
     * Update user login time through ID
     *
     * @param id Number
     *
     * @return {@link Boolean}
     */
    @Update("update user set last_login_time=current_timestamp where id=#{id}")
    boolean updateUserLoginTime(int id);

    /**
     * Update operation user permissions
     *
     * @param id user id
     * @param isDownloadable Download permissions
     * @param isUploadable Upload permissions
     * @param isVisible Searchable permissions
     * @param isDeletable Remove permissions
     * @param isUpdatable Update permissions
     *
     * @return {@link Boolean}
     */
    @UpdateProvider(type = UserSqlProvider.class, method = "updateAuthById")
    boolean updateAuthById(@Param("id") int id, @Param("isDownloadable") int isDownloadable,
                           @Param("isUploadable") int isUploadable, @Param("isDeletable") int isDeletable, @Param(
                                   "isUpdatable") int isUpdatable, @Param("isVisible") int isVisible);

    /**
     * Update password by number
     *
     * @param id Number
     * @param password Password
     *
     * @return {@link Boolean}
     */
    @Update("update user set password=sha2(#{password},256) where id=#{id}")
    boolean updatePasswordById(@Param("id") int id, @Param("password") String password);

    /**
     * Update password through email
     *
     * @param password Password
     * @param email Mailbox
     *
     * @return {@link Boolean}
     */
    @Update("update user set password=sha2(#{password},256) where email=#{email}")
    boolean updatePasswordByEmail(@Param("password") String password, @Param("email") String email);

    /**
     * Check username
     *
     * @param username user name
     *
     * @return {@link Integer}
     */
    @Select("select count(*) from user where username=#{username}")
    int checkUsername(String username);

    /**
     * Check email
     *
     * @param email Mailbox
     *
     * @return {@link Integer}
     */
    @Select("select count(*) from user where email=#{email}")
    int checkEmail(String email);
}
