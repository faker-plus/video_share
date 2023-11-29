package com.video.web.controller;

import com.video.annotation.AuthInterceptor;
import com.video.enums.InterceptorLevel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @author tian
 * @since 2023/11/25
 */
@Controller
@Api(description = "View Page Mapping")
public class ViewController {

    @ApiOperation(value = "Remote File Management Page")
    @AuthInterceptor(InterceptorLevel.SYSTEM)
    @RequestMapping(value = "/filemanager", method = RequestMethod.GET)
    public String fileManager() {
        return "/filemanager";
    }

    @ApiOperation(value = "Upload Page")
    @AuthInterceptor
    @RequestMapping(value = "/upload", method = RequestMethod.GET)
    public String upload() {
        return "/upload";
    }

    @ApiOperation(value = "HOME PAGE")
    @AuthInterceptor(InterceptorLevel.NONE)
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String index() {
        return "/index";
    }

    @ApiOperation(value = "Login, Registration, Forgot Password Page")
    @AuthInterceptor(InterceptorLevel.NONE)
    @RequestMapping(value = "/signin", method = RequestMethod.GET)
    public String signin() {
        return "/signin";
    }

    @ApiOperation(value = "Administrator page")
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public String admin() {
        return "/admin";
    }

    @ApiIgnore
    @AuthInterceptor(InterceptorLevel.NONE)
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    @ResponseBody
    public String test() {
        return "<b>test</b>";
    }
}
