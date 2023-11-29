package com.video.web.controller;

import com.video.annotation.AuthInterceptor;
import com.video.service.IUploadedService;
import com.video.enums.InterceptorLevel;
import com.zhazhapan.util.Formatter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author tian
 * @since 2018/2/28
 */
@RestController
@RequestMapping(value = "/uploaded")
@Api(value = "/uploaded", description = "Upload Record Related Operations")
public class UploadedController {

    private final IUploadedService uploadedService;

    @Autowired
    public UploadedController(IUploadedService uploadedService) {this.uploadedService = uploadedService;}

    @ApiOperation(value = "Obtain file upload records")
    @ApiImplicitParams({@ApiImplicitParam(name = "user", value = "Specify users (default to all users)"), @ApiImplicitParam(name =
            "Specify files (default for all files)"), @ApiImplicitParam(name = "category", value = "Specify classification (default for all classifications)"), @ApiImplicitParam(name =
            "offset", value = "Offset", required = true)})
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "all", method = RequestMethod.GET)
    public String getAll(String user, String file, String category, int offset) {
        return Formatter.listToJson(uploadedService.list(user, file, category, offset));
    }
}
