package com.video.web.controller;

import com.video.annotation.AuthInterceptor;
import com.video.service.IAuthService;
import com.video.enums.InterceptorLevel;
import com.video.util.ControllerUtils;
import com.zhazhapan.util.Formatter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author tian
 * @since 2018/3/8
 */
@RestController
@RequestMapping("/auth")
@Api(value = "/auth", description = "Permission Table Related Operations")
public class AuthController {

    private final IAuthService authService;

    @Autowired
    public AuthController(IAuthService authService) {this.authService = authService;}

    @ApiOperation(value = "Add permission record", notes = "Set permissions for specified users on specified files")
    @ApiImplicitParams({@ApiImplicitParam(name = "files", value = "文件", example = "file1,file2,file3", required = true),
            @ApiImplicitParam(name = "users", value = "User", example = "user1,user2,user3", required = true),
            @ApiImplicitParam(name = "auths", value = "authority", example = "1,1,1,1", required = true)})
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "", method = RequestMethod.POST)
    public String add(String files, String users, String auths) {
        System.out.println("files: " + files + " users: " + users + " auths: " + auths);
        return ControllerUtils.getResponse(authService.addAuth(files, users, auths));
    }

    @ApiOperation(value = "Obtain permission records")
    @ApiImplicitParams({@ApiImplicitParam(name = "user", value = "用户", required = true), @ApiImplicitParam(name =
            "file", value = "文件", required = true), @ApiImplicitParam(name = "offset", value = "偏移量", required = true)})
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public String getAuth(String user, String file, int offset) {
        return Formatter.listToJson(authService.listAuth(user, file, offset));
    }

    @ApiOperation(value = "更新权限记录")
    @ApiImplicitParams({@ApiImplicitParam(name = "auth", value = "权限值", required = true)})
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public String updateAuth(@PathVariable("id") long id, String auth) {
        return ControllerUtils.getResponse(authService.updateAuth(id, auth));
    }

    @ApiOperation(value = "批量删除权限记录")
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/batch/{ids}", method = RequestMethod.DELETE)
    public String batchDelete(@PathVariable("ids") String ids) {
        return ControllerUtils.getResponse(authService.batchDelete(ids));
    }
}
