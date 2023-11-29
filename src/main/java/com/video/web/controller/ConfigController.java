package com.video.web.controller;

import com.video.VideoApplication;
import com.video.annotation.AuthInterceptor;
import com.video.entity.User;
import com.video.modules.constant.DefaultValues;
import com.video.service.IConfigService;
import com.video.enums.InterceptorLevel;
import com.zhazhapan.modules.constant.ValueConsts;
import com.zhazhapan.util.FileExecutor;
import com.zhazhapan.util.NetUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author tian
 * @since 2023/11/22
 */
@RestController
@RequestMapping("/config")
@Api(value = "/config", description = "配置文件的相关操作")
public class ConfigController {

    private static Logger logger = Logger.getLogger(ConfigController.class);

    private final IConfigService configService;

    private final HttpServletRequest request;

    @Autowired
    public ConfigController(IConfigService configService, HttpServletRequest request) {
        this.configService = configService;
        this.request = request;
    }

    @ApiOperation(value = "更新配置文件")
    @ApiImplicitParam(name = "config", value = "配置文件内容", required = true)
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public String updateConfig(String config) {
        User user = (User) request.getSession().getAttribute(ValueConsts.USER_STRING);
        if (user.getPermission() > ValueConsts.TWO_INT) {
            VideoApplication.settings.setJsonObject(config);
            //打包成jar之后无法修改config.json文件
            try {
                FileExecutor.saveFile(NetUtils.urlToString(VideoApplication.class.getResource(DefaultValues
                        .SETTING_PATH)), VideoApplication.settings.toString());
            } catch (IOException e) {
                logger.error(e.getMessage());
                return "{\"message\":\"internal error, cannot save\"}";
            }
            return "{\"message\":\"saved successfully\"}";
        } else {
            return "{\"message\":\"permission denied\"}";
        }
    }

    @ApiOperation(value = "获取配置文件内容")
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public String getAll() {
        User user = (User) request.getSession().getAttribute(ValueConsts.USER_STRING);
        if (user.getPermission() > ValueConsts.TWO_INT) {
            return VideoApplication.settings.toString();
        } else {
            return "{\"message\":\"permission denied\"}";
        }
    }

    @ApiOperation(value = "获取配置文件中的全局相关配置内容")
    @AuthInterceptor(InterceptorLevel.NONE)
    @RequestMapping(value = "/global", method = RequestMethod.GET)
    public String getGlobalConfig() {
        return configService.getGlobalConfig();
    }

    @ApiOperation(value = "获取配置文件中的用户相关配置内容")
    @AuthInterceptor(InterceptorLevel.NONE)
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public String getUserConfig() {
        return configService.getUserConfig();
    }
}
