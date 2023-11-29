package com.video.dao;

import com.video.dao.sqlprovider.UploadedSqlProvider;
import com.video.model.UploadedRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author tian
 * @since 2018/2/28
 */
@Repository
public interface UploadedDAO {

    /**
     * Query upload records
     *
     * @param userId User ID, set the value to be less than or equal to 0 when not using the user ID as a condition
     * @param fileId File number, set the value to be less than or equal to 0 when not using file number as a condition
     * @param categoryId Category number, set a value less than or equal to 0 when no category number is used as a condition
     * @param fileName File name, set the value to blank when not using a file name as a condition
     * @param offset Deviation
     *
     * @return Upload Record
     */
    @SelectProvider(type = UploadedSqlProvider.class, method = "getDownloadBy")
    List<UploadedRecord> listUploadedBy(@Param("userId") int userId, @Param("fileId") long fileId, @Param("fileName")
            String fileName, @Param("categoryId") int categoryId, @Param("offset") int offset);
}
