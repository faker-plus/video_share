package com.video.service.impl;

import com.video.VideoApplication;
import com.video.config.SettingConfig;
import com.video.config.TokenConfig;
import com.video.entity.User;
import com.video.modules.constant.ConfigConsts;
import com.video.service.IUserService;
import com.video.dao.UserDAO;
import com.video.util.BeanUtils;
import com.zhazhapan.modules.constant.ValueConsts;
import com.zhazhapan.util.Checker;
import com.zhazhapan.util.DateUtils;
import com.zhazhapan.util.MailSender;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author tian
 * @since 2023/11/22
 */
@Service
public class UserServiceImpl implements IUserService {

    private final UserDAO userDAO;

    private Logger logger = Logger.getLogger(UserServiceImpl.class);

    @Autowired
    public UserServiceImpl(UserDAO userDAO) {this.userDAO = userDAO;}

    @Override
    public boolean updatePermission(int id, int permission) {
        return userDAO.updatePermission(id, permission > 2 ? 2 : permission);
    }

    @Override
    public boolean resetPassword(int id, String password) {
        boolean result = Checker.isNotEmpty(password) && userDAO.updatePasswordById(id, password);
        if (result) {
            TokenConfig.removeTokenByValue(id);
            try {
                MailSender.sendMail(getUserById(id).getEmail(), "Password reset notification", "Your password has been reset by the administrator to：" + password);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return result;
    }

    @Override
    public boolean updateFileAuth(int id, String auths) {
        int[] auth = BeanUtils.getAuth(auths);
        return userDAO.updateAuthById(id, auth[0], auth[1], auth[2], auth[3], auth[4]);
    }

    @Override
    public List<User> listUser(int permission, String condition, int offset) {
        return userDAO.listUserBy(permission, condition, offset);
    }

    @Override
    public User login(String loginName, String password, String token, HttpServletResponse response) {
        boolean allowLogin = VideoApplication.settings.getBooleanUseEval(ConfigConsts.ALLOW_LOGIN_OF_SETTINGS);
        User user = null;
        if (allowLogin) {
            if (Checker.isNotEmpty(token) && VideoApplication.tokens.containsKey(token)) {
                user = userDAO.getUserById(VideoApplication.tokens.get(token));
                if (Checker.isNotNull(response)) {
                    Cookie cookie = new Cookie(ValueConsts.TOKEN_STRING, TokenConfig.generateToken(token, user.getId
                            ()));
                    cookie.setMaxAge(30 * 24 * 60 * 60);
                    response.addCookie(cookie);
                }
            }
            if (Checker.isNull(user) && Checker.isNotEmpty(loginName) && Checker.isNotEmpty(password)) {
                user = userDAO.login(loginName, password);
                if (Checker.isNotNull(user)) {
                    TokenConfig.removeTokenByValue(user.getId());
                }
            }
            updateUserLoginTime(user);
        }
        return user;
    }

    @Override
    public boolean register(String username, String email, String password) {
        boolean allowRegister = VideoApplication.settings.getBooleanUseEval(ConfigConsts.ALLOW_REGISTER_OF_SETTINGS);
        if (allowRegister) {
            boolean isValid = Checker.isEmail(email) && checkPassword(password) && Pattern.compile(VideoApplication.settings
                    .getStringUseEval(ConfigConsts.USERNAME_PATTERN_OF_SETTINGS)).matcher(username).matches();
            if (isValid) {
                User user = new User(username, ValueConsts.EMPTY_STRING, email, password);
                int[] auth = SettingConfig.getAuth(ConfigConsts.USER_DEFAULT_AUTH_OF_SETTING);
                user.setAuth(auth[0], auth[1], auth[2], auth[3], auth[4]);
                return userDAO.insertUser(user);
            }
        }
        return false;
    }

    @Override
    public boolean resetPasswordByEmail(String email, String password) {
        return Checker.isEmail(email) && checkPassword(password) && userDAO.updatePasswordByEmail(password, email);
    }

    @Override
    public boolean checkPassword(String password) {
        int min = VideoApplication.settings.getIntegerUseEval(ConfigConsts.PASSWORD_MIN_LENGTH_OF_SETTINGS);
        int max = VideoApplication.settings.getIntegerUseEval(ConfigConsts.PASSWORD_MAX_LENGTH_OF_SETTINGS);
        return Checker.isLimited(password, min, max);
    }

    @Override
    public boolean emailExists(String email) {
        return Checker.isEmail(email) && userDAO.checkEmail(email) > 0;
    }

    @Override
    public boolean updateBasicInfoById(int id, String avatar, String realName, String email) {
        return Checker.isEmail(email) && userDAO.updateBasicInfo(id, Checker.checkNull(avatar), Checker.checkNull
                (realName), email);
    }

    @Override
    public int getUserId(String usernameOrEmail) {
        try {
            return userDAO.getUserId(Checker.checkNull(usernameOrEmail));
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public boolean usernameExists(String username) {
        return Checker.isNotEmpty(username) && userDAO.checkUsername(username) > 0;
    }

    @Override
    public User getUserById(int id) {
        return userDAO.getUserById(id);
    }

    @Override
    public void updateUserLoginTime(User user) {
        if (Checker.isNotNull(user)) {
            user.setLastLoginTime(DateUtils.getCurrentTimestamp());
            userDAO.updateUserLoginTime(user.getId());
        }
    }

    @Override
    public boolean updatePasswordById(String password, int id) {
        return checkPassword(password) && userDAO.updatePasswordById(id, password);
    }
}
