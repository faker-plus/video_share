package com.video.web.controller;

import com.video.annotation.AuthInterceptor;
import com.video.enums.InterceptorLevel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @author tian
 * @since 2023/11/22
 */
@Controller
@Api(description = "错误页面映射")
public class CustomErrorController implements ErrorController {

    @ApiOperation(value = "异常页面")
    @AuthInterceptor(InterceptorLevel.NONE)
    @RequestMapping("/exception")
    public String handleError() {
        return "error";
    }

    @ApiOperation(value = "404、错误页面")
    @AuthInterceptor(InterceptorLevel.NONE)
    @RequestMapping("/error")
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound() {
        return "/404";
    }

    @ApiIgnore
    @Override
    public String getErrorPath() {
        return "/error";
    }
}