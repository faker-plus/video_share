package com.video.web.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.video.VideoApplication;
import com.video.annotation.AuthInterceptor;
import com.video.entity.User;
import com.video.modules.constant.ConfigConsts;
import com.video.service.IFileService;
import com.video.enums.InterceptorLevel;
import com.video.util.BeanUtils;
import com.video.util.ControllerUtils;
import com.zhazhapan.modules.constant.ValueConsts;
import com.zhazhapan.util.Checker;
import com.zhazhapan.util.FileExecutor;
import com.zhazhapan.util.Formatter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 * @author tian
 * @since 2023/11/29
 */
@RestController
@RequestMapping("/file")
@Api(value = "/file", description = "文件相关操作")
public class FileController {

    private final IFileService fileService;

    private final HttpServletRequest request;

    private final JSONObject jsonObject;

    @Autowired
    public FileController(IFileService fileService, HttpServletRequest request, JSONObject jsonObject) {
        this.fileService = fileService;
        this.request = request;
        this.jsonObject = jsonObject;
    }



    @ApiOperation(value = "Get my upload records")
    @ApiImplicitParams({@ApiImplicitParam(name = "offset", value = "Offset", required = true), @ApiImplicitParam(name =
            "search", value = "Record matching (allowed to be empty)")})
    @AuthInterceptor(InterceptorLevel.USER)
    @RequestMapping(value = "/user/uploaded", method = RequestMethod.GET)
    public String getUserUploaded(int offset, String search) {
        User user = (User) request.getSession().getAttribute(ValueConsts.USER_STRING);
        return Formatter.listToJson(fileService.listUserUploaded(user.getId(), offset, search));
    }

    @ApiOperation(value = "File upload")
    @ApiImplicitParams({@ApiImplicitParam(name = "categoryId", value = "Classification ID", required = true), @ApiImplicitParam
            (name = "tag", value = "FILE LABEL"), @ApiImplicitParam(name = "description", value = "文件描述"),
            @ApiImplicitParam(name = "prefix", value = "File prefix (only applicable to administrators uploading files, invalid for regular users)")})
    @AuthInterceptor(InterceptorLevel.USER)
    @RequestMapping(value = "", method = RequestMethod.POST)
    public String upload(int categoryId, String tag, String description, String prefix, @RequestParam("file")
            MultipartFile multipartFile) {
        User user = (User) request.getSession().getAttribute(ValueConsts.USER_STRING);
        return ControllerUtils.getResponse(fileService.upload(categoryId, tag, description, prefix, multipartFile,
                user));
    }

    @ApiOperation(value = "获取文件记录")
    @ApiImplicitParams({@ApiImplicitParam(name = "offset", value = "偏移量", required = true), @ApiImplicitParam(name =
            "categoryId", value = "分类ID", required = true), @ApiImplicitParam(name = "orderBy", value = "排序方式",
            required = true, example = "id desc"), @ApiImplicitParam(name = "search", value = "记录匹配（允许为空）")})
    @AuthInterceptor(InterceptorLevel.NONE)
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public String getAll(int offset, int categoryId, String orderBy, String search) {
        User user = (User) request.getSession().getAttribute(ValueConsts.USER_STRING);
        boolean canGet = VideoApplication.settings.getBooleanUseEval(ConfigConsts.ANONYMOUS_VISIBLE_OF_SETTING) ||
                (Checker.isNotNull(user) && user.getIsVisible() == 1);
        if (canGet) {
            int userId = Checker.isNull(user) ? 0 : user.getId();
            return Formatter.listToJson(fileService.listAll(userId, offset, categoryId, orderBy, search));
        } else {
            jsonObject.put("error", "权限被限制，无法获取资源，请联系管理员");
            return jsonObject.toString();
        }
    }

    @ApiOperation(value = "Delete specified file")
    @AuthInterceptor(InterceptorLevel.USER)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String removeFile(@PathVariable("id") long id) {
        User user = (User) request.getSession().getAttribute(ValueConsts.USER_STRING);
        jsonObject.put("status", "error");
        if (Checker.isNull(user)) {
            jsonObject.put("message", "Please log in first");
        } else if (id < 1) {
            jsonObject.put("message", "Format is illegal");
        } else if (fileService.removeFile(user, id)) {
            jsonObject.put("status", "success");
        } else {
            jsonObject.put("message", "Deletion failed, insufficient permissions. Please contact the administrator");
        }
        return jsonObject.toString();
    }

    @ApiOperation(value = "Update File Properties")
    @ApiImplicitParams({@ApiImplicitParam(name = "name", value = "FILE NAME", required = true), @ApiImplicitParam(name =
            "category", value = "Classification name", required = true), @ApiImplicitParam(name = "tag", value = "FILE LABEL", required =
            true), @ApiImplicitParam(name = "description", value = "文件描述", required = true)})
    @AuthInterceptor(InterceptorLevel.USER)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public String updateFileInfo(@PathVariable("id") long id, String name, String category, String tag, String
            description) {
        User user = (User) request.getSession().getAttribute(ValueConsts.USER_STRING);
        jsonObject.put("status", "error");
        if (fileService.updateFileInfo(id, user, name, category, tag, description)) {
            jsonObject.put("status", "success");
        } else {
            jsonObject.put("message", "Incorrect format or insufficient permissions, update failed. Please contact the administrator");
        }
        return jsonObject.toString();
    }

    @ApiOperation(value = "Obtain basic information about all files")
    @ApiImplicitParams({@ApiImplicitParam(name = "user", value = "指定用户（默认所有用户）"), @ApiImplicitParam(name = "file",
            value = "指定文件（默认所有文件）"), @ApiImplicitParam(name = "category", value = "指定分类（默认所有分类）"), @ApiImplicitParam
            (name = "offset", value = "偏移量", required = true)})
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/basic/all", method = RequestMethod.GET)
    public String getBasicAll(String user, String file, String category, int offset) {
        return Formatter.listToJson(fileService.listBasicAll(user, file, category, offset));
    }

    @ApiOperation(value = "Obtain server-side files through file path")
    @ApiImplicitParam(name = "path", value = "File path (default root directory)")
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/server", method = RequestMethod.GET)
    public String getServerFilesByPath(String path) {
        File[] files = FileExecutor.listFile(Checker.isEmpty(path) ? (Checker.isWindows() ? "C:\\" : "/") : path);
        JSONArray array = new JSONArray();
        if (Checker.isNotNull(files)) {
            for (File file : files) {
                array.add(BeanUtils.beanToJson(file));
            }
        }
        return array.toJSONString();
    }

    @ApiOperation("Share server-side files")
    @ApiImplicitParams({@ApiImplicitParam(name = "prefix", value = "Custom prefix (can be empty)"), @ApiImplicitParam(name = "files",
            value = "File", required = true, example = "file1,file2,file3")})
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/server/share", method = RequestMethod.POST)
    public String shareFile(String prefix, String files) {
        User user = (User) request.getSession().getAttribute(ValueConsts.USER_STRING);
        return ControllerUtils.getResponse(fileService.shareFiles(Checker.checkNull(prefix), files, user));
    }

    @ApiOperation(value = "Update the file path (including local path and access path. If the new local path and access path are both empty, nothing will be done)")
    @ApiImplicitParams({@ApiImplicitParam(name = "oldLocalUrl", value = "文件本地路径", required = true), @ApiImplicitParam
            (name = "localUrl", value = "新的本地路径（可空）"), @ApiImplicitParam(name = "visitUrl", value = "新的访问路径（可空）")})
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/{id}/url", method = RequestMethod.PUT)
    public String uploadFileUrl(@PathVariable("id") int id, String oldLocalUrl, String localUrl, String visitUrl) {
        boolean[] b = fileService.updateUrl(id, oldLocalUrl, localUrl, visitUrl);
        String responseJson = "{status:{localUrl:" + b[0] + ",visitUrl:" + b[1] + "}}";
        return Formatter.formatJson(responseJson);
    }

    @ApiOperation(value = "批量删除文件")
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/batch/{ids}", method = RequestMethod.DELETE)
    public String deleteFiles(@PathVariable("ids") String ids) {
        return ControllerUtils.getResponse(fileService.deleteFiles(ids));
    }

    @ApiOperation(value = "获取指定文件的权限记录")
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/{id}/auth", method = RequestMethod.GET)
    public String getAuth(@PathVariable("id") long id) {
        return BeanUtils.toPrettyJson(fileService.getAuth(id));
    }

    @ApiOperation(value = "更新指定文件的权限")
    @ApiImplicitParam(name = "auth", value = "权限", required = true, example = "1,1,1,1")
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/{id}/auth", method = RequestMethod.PUT)
    public String updateAuth(@PathVariable("id") long id, String auth) {
        return ControllerUtils.getResponse(fileService.updateAuth(id, auth));
    }

    // /**
    //  * 资源下载
    //  *
    //  * @param response {@link HttpServletResponse}
    //  */
    // @ApiOperation(value = "通过访问路径获取文件资源")
    // @AuthInterceptor(InterceptorLevel.NONE)
    // @RequestMapping(value = "/**", method = RequestMethod.GET)
    // public void getResource(HttpServletResponse response) throws IOException {
    //     ControllerUtils.loadResource(response, fileService.getResource(request.getServletPath(), request),
    //             ValueConsts.FALSE);
    // }
}
