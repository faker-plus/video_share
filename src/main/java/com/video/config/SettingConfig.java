package com.video.config;

import com.video.VideoApplication;
import com.video.modules.constant.ConfigConsts;
import com.video.util.CommonUtils;
import com.zhazhapan.modules.constant.ValueConsts;
import com.zhazhapan.util.Checker;
import com.zhazhapan.util.FileExecutor;
import com.zhazhapan.util.Formatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static com.video.VideoApplication.settings;

/**
 * @author tian
 * @since 2023/11/26
 */
public class SettingConfig {

    private static final String WINDOWS = "windows";

    private static final String MAC = "mac";

    private static final String LINUX = "linux";

    private static Logger logger = LoggerFactory.getLogger(SettingConfig.class);

    private static OsName currentOS;

    static {
        if (Checker.isWindows()) {
            currentOS = OsName.WINDOWS;
        } else if (Checker.isMacOS()) {
            currentOS = OsName.MAC;
        } else {
            currentOS = OsName.LINUX;
        }
    }

    public static int[] getAuth(String jsonPath) {
        int[] auth = new int[5];
        for (int i = 0; i < ConfigConsts.AUTH_OF_SETTINGS.length; i++) {
            String key = jsonPath + ValueConsts.DOT_SIGN + ConfigConsts.AUTH_OF_SETTINGS[i];
            auth[i] = settings.getBooleanUseEval(key) ? 1 : 0;
        }
        return auth;
    }

    public static String getUploadStoragePath() {
        String parent = getStoragePath(ConfigConsts.UPLOAD_PATH_OF_SETTING);
        String formatWay = VideoApplication.settings.getStringUseEval(ConfigConsts.UPLOAD_FORM_OF_SETTING);
        String childPath = ValueConsts.SEPARATOR + Formatter.datetimeToCustomString(new Date(), formatWay);
        String path = parent + childPath;
        if (!FileExecutor.createFolder(path)) {
            path = ConfigConsts.DEFAULT_UPLOAD_PATH + childPath;
            FileExecutor.createFolder(path);
        }
        logger.info("upload path: " + path);
        return path;
    }

    public static String getAvatarStoragePath() {
        String path = getStoragePath(ConfigConsts.UPLOAD_PATH_OF_SETTING) + ValueConsts.SEPARATOR + "avatar";
        FileExecutor.createFolder(path);
        return path;
    }

    public static String getStoragePath(String path) {
        path += ValueConsts.DOT_SIGN;
        if (currentOS == OsName.WINDOWS) {
            path += WINDOWS;
        } else if (currentOS == OsName.MAC) {
            path += MAC;
        } else {
            path += LINUX;
        }
        return CommonUtils.checkPath(VideoApplication.settings.getStringUseEval(path));
    }

    /**
     * 当前系统名称Current system name
     */
    public enum OsName {
        /**
         * Windows system
         */
        WINDOWS,

        /**
         * Mac OS system
         */
        MAC,

        /**
         * Linux system
         */
        LINUX
    }
}
