package com.video.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.video.annotation.AuthInterceptor;
import com.video.config.TokenConfig;
import com.video.entity.User;
import com.video.modules.constant.ConfigConsts;
import com.video.service.IUserService;
import com.video.VideoApplication;
import com.video.enums.InterceptorLevel;
import com.video.modules.constant.DefaultValues;
import com.video.util.ControllerUtils;
import com.zhazhapan.modules.constant.ValueConsts;
import com.zhazhapan.util.Checker;
import com.zhazhapan.util.Formatter;
import com.zhazhapan.util.encryption.JavaEncrypt;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * @author tian
 * @since 2023/11/22
 */
@RestController
@RequestMapping("/user")
@Api(value = "/user", description = "User related operations")
public class UserController {

    private final IUserService userService;

    private final HttpServletRequest request;

    private final JSONObject jsonObject;

    @Autowired
    public UserController(IUserService userService, HttpServletRequest request, JSONObject jsonObject) {
        this.userService = userService;
        this.request = request;
        this.jsonObject = jsonObject;
    }

    @ApiOperation(value = "Update user permissions (note: not file permissions)")
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/{id}/{permission}", method = RequestMethod.PUT)
    public String updatePermission(@PathVariable("id") int id, @PathVariable("permission") int permission) {
        User user = (User) request.getSession().getAttribute(ValueConsts.USER_STRING);
        if (user.getPermission() < ValueConsts.THREE_INT && permission > 1) {
            jsonObject.put("message", "Insufficient permissions, setting failed");
        } else if (userService.updatePermission(id, permission)) {
            jsonObject.put("message", "Update successful");
        } else {
            jsonObject.put("message", "Update failed, please try again later");
        }
        return jsonObject.toJSONString();
    }

    @ApiOperation("Reset User Password (Administrator Interface)")
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/reset/{id}/{password}", method = RequestMethod.PUT)
    public String resetPassword(@PathVariable("id") int id, @PathVariable("password") String password) {
        return ControllerUtils.getResponse(userService.resetPassword(id, password));
    }

    @ApiOperation(value = "Update users' default file permissions")
    @ApiImplicitParam(name = "auth", value = "AUTHORITY", example = "1,1,1,1", required = true)
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/{id}/auth", method = RequestMethod.PUT)
    public String updateFileAuth(@PathVariable("id") int id, String auth) {
        return ControllerUtils.getResponse(userService.updateFileAuth(id, auth));
    }

    @ApiOperation(value = "Get all users")
    @ApiImplicitParams({@ApiImplicitParam(name = "user", value = "Specify users (default to all users)"), @ApiImplicitParam(name = "offset",
            value = "Offset", required = true)})
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public String getUser(String user, int offset) {
        User u = (User) request.getSession().getAttribute(ValueConsts.USER_STRING);
        return Formatter.listToJson(userService.listUser(u.getPermission(), user, offset));
    }

    @ApiOperation(value = "Update my basic information")
    @ApiImplicitParams({@ApiImplicitParam(name = "avatar", value = "Head portrait (can be empty)"), @ApiImplicitParam(name = "realName",
            value = "Real name (can be blank)"), @ApiImplicitParam(name = "email", value = "Email (can be empty)"), @ApiImplicitParam(name =
            "code", value = "Verification code (can be blank)")})
    @AuthInterceptor(InterceptorLevel.USER)
    @RequestMapping(value = "/info", method = RequestMethod.PUT)
    public String updateBasicInfo(String avatar, String realName, String email, String code) {
        User user = (User) request.getSession().getAttribute(ValueConsts.USER_STRING);
        jsonObject.put("message", "Successfully saved");
        boolean emilVerify = VideoApplication.settings.getBooleanUseEval(ConfigConsts.EMAIL_VERIFY_OF_SETTINGS);
        if (Checker.isNotEmpty(email) && !email.equals(user.getEmail())) {
            if (!emilVerify || isCodeValidate(code)) {
                if (userService.emailExists(email)) {
                    jsonObject.put("message", "Email update failed, the email already exists");
                } else {
                    user.setEmail(email);
                }
            } else {
                jsonObject.put("message", "Email update failed, verification code verification failed");
            }
        }
        if (userService.updateBasicInfoById(user.getId(), avatar, realName, user.getEmail())) {
            user.setAvatar(avatar);
            user.setRealName(realName);
            jsonObject.put("status", "success");
        } else {
            jsonObject.put("message", "The server encountered an error, please try again later");
        }
        jsonObject.put("email", user.getEmail());
        return jsonObject.toString();
    }

    @ApiOperation(value = "Update my password")
    @ApiImplicitParams({@ApiImplicitParam(name = "oldPassword", value = "Original password", required = true), @ApiImplicitParam
            (name = "newPassword", value = "New password", required = true)})
    @AuthInterceptor(InterceptorLevel.USER)
    @RequestMapping(value = "/password", method = RequestMethod.PUT)
    public String updatePassword(String oldPassword, String newPassword) {
        User user = (User) request.getSession().getAttribute(ValueConsts.USER_STRING);
        jsonObject.put("status", "error");
        try {
            if (user.getPassword().equals(JavaEncrypt.sha256(oldPassword))) {
                if (userService.updatePasswordById(newPassword, user.getId())) {
                    jsonObject.put("status", "success");
                    TokenConfig.removeTokenByValue(user.getId());
                } else {
                    jsonObject.put("message", "The new password format is incorrect");
                }
            } else {
                jsonObject.put("message", "The original password is incorrect");
            }
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            jsonObject.put("message", "Internal server error, please try again later");
        }
        return jsonObject.toString();
    }

    @ApiOperation(value = "Get my basic information")
    @AuthInterceptor(InterceptorLevel.USER)
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public String getInfo() {
        User user = (User) request.getSession().getAttribute(ValueConsts.USER_STRING);
        JSONObject object = JSON.parseObject(user.toString());
        object.remove(ValueConsts.ID_STRING);
        object.remove(ValueConsts.PASSWORD_STRING);
        return object.toString();
    }

    @ApiOperation(value = "Login (username, password, and token must have one input)")
    @ApiImplicitParams({@ApiImplicitParam(name = "username", value = "USER NAME"), @ApiImplicitParam(name
            = "password", value = "Password"), @ApiImplicitParam(name = "auto", value = "Automatically log in or not", dataType = "Boolean"),
            @ApiImplicitParam(name = "token", value = "For automatic login")})
    @AuthInterceptor(InterceptorLevel.NONE)
    @RequestMapping(value = "/login", method = RequestMethod.PUT)
    public String login(String username, String password, boolean auto, String token) {
        //Log in with password
        User user = userService.login(username, password, ValueConsts.NULL_STRING, ValueConsts.NULL_RESPONSE);
        if (Checker.isNull(user) || user.getPermission() < 1) {
            jsonObject.put("status", "failed");
        } else {
            request.getSession().setAttribute(ValueConsts.USER_STRING, user);
            jsonObject.put("status", "success");
            if (auto) {
                jsonObject.put("token", TokenConfig.generateToken(token, user.getId()));
            } else {
                jsonObject.put("token", "");
                TokenConfig.removeTokenByValue(user.getId());
            }
        }
        return jsonObject.toString();
    }

    @ApiOperation(value = "User registration (when there is no need to verify the email, the email and verification code can be blank)")
    @ApiImplicitParams({@ApiImplicitParam(name = "username", value = "user name", required = true), @ApiImplicitParam(name
            = "email", value = "Mailbox"), @ApiImplicitParam(name = "password", value = "Password", required = true),
            @ApiImplicitParam(name = "code", value = "Verification code")})
    @AuthInterceptor(InterceptorLevel.NONE)
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(String username, String email, String password, String code) {
        boolean emilVerify = VideoApplication.settings.getBooleanUseEval(ConfigConsts.EMAIL_VERIFY_OF_SETTINGS);
        jsonObject.put("status", "error");
        if (!emilVerify || isCodeValidate(code)) {
            if (userService.usernameExists(username)) {
                jsonObject.put("message", "The username already exists");
            } else if (userService.emailExists(email)) {
                jsonObject.put("message", "This email has already been registered");
            } else if (userService.register(username, email, password)) {
                jsonObject.put("status", "success");
            } else {
                jsonObject.put("message", "Illegal data format");
            }
        } else {
            jsonObject.put("message", "Verification code verification failed");
        }
        return jsonObject.toString();
    }

    @ApiOperation(value = "Reset my password")
    @ApiImplicitParams({@ApiImplicitParam(name = "email", value = "Mailbox", required = true), @ApiImplicitParam(name =
            "code", value = "Verification code", required = true), @ApiImplicitParam(name = "password", value = "Password", required =
            true)})
    @AuthInterceptor(InterceptorLevel.NONE)
    @RequestMapping(value = "/password/reset", method = RequestMethod.PUT)
    public String resetPassword(String email, String code, String password) {
        jsonObject.put("status", "error");
        if (isCodeValidate(code)) {
            if (userService.resetPasswordByEmail(email, password)) {
                jsonObject.put("status", "success");
            } else {
                jsonObject.put("message", "Format is illegal");
            }
        } else {
            jsonObject.put("message", "Verification code verification failed");
        }
        return jsonObject.toString();
    }

    @ApiOperation(value = "Check if the username has been registered")
    @ApiImplicitParam(name = "username", value = "USER NAME", required = true)
    @AuthInterceptor(InterceptorLevel.NONE)
    @RequestMapping(value = "/username/exists", method = RequestMethod.GET)
    public String usernameExists(String username) {
        jsonObject.put("exists", userService.usernameExists(username));
        return jsonObject.toString();
    }

    @ApiOperation(value = "Check if the email has been registered")
    @ApiImplicitParam(name = "email", value = "Mailbox", required = true)
    @AuthInterceptor(InterceptorLevel.NONE)
    @RequestMapping(value = "/email/exists", method = RequestMethod.GET)
    public String emailExists(String email) {
        jsonObject.put("exists", userService.emailExists(email));
        return jsonObject.toString();
    }

    private boolean isCodeValidate(String code) {
        return Checker.checkNull(code).equals(String.valueOf(request.getSession().getAttribute(DefaultValues
                .CODE_STRING)));
    }
}
