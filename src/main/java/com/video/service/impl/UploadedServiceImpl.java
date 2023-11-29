package com.video.service.impl;

import com.video.model.UploadedRecord;
import com.video.service.IUploadedService;
import com.video.dao.UploadedDAO;
import com.video.util.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author tian
 * @since 2018/2/28
 */
@Service
public class UploadedServiceImpl implements IUploadedService {

    private final UploadedDAO uploadedDAO;

    @Autowired
    public UploadedServiceImpl(UploadedDAO uploadedDAO) {this.uploadedDAO = uploadedDAO;}

    @SuppressWarnings("unchecked")
    @Override
    public List<UploadedRecord> list(String user, String file, String category, int offset) {
        return (List<UploadedRecord>) ServiceUtils.invokeFileFilter(uploadedDAO, "listUploadedBy", user, file,
                category, offset);
    }
}
