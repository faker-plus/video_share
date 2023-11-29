var signinItem = new Vue({
    el: "#signin-div",
    data: {
        emailVerify: false,
        description: "",
        emailErrorTip: "",
        emailVerifyStatus: "",
        passwordVerify: "",
        passwordConfirm: ""
    }
});

if (!window.location.href.replace(/https?:\/\/[a-zA-Z0-9.]*(:\d+)?/g, "").startsWith("/signin")) {
    window.location.href = "/signin#login";
}

function reset() {
    var email = $("#res-email").val();
    var code = $("#res-email-verify").val();
    var password = $("#res-password").val();
    var passwordConfirm = $("#res-confirm-password").val();
    var isValid = isEmail(email) && 6 === code.length && checkPassword(password, passwordConfirm);
    if (submit() && isValid) {
        layer.load(1);
        $.ajax({
            url: "/user/password/reset",
            type: "PUT",
            data: {email: email, code: code, password: password},
            success: function (data) {
                layer.closeAll();
                var json = JSON.parse(data);
                if (json.status === "success") {
                    alerts("Password reset successful");
                    switchToLogin();
                } else {
                    alerts(json.message);
                }
            }
        });
    } else {
        alerts("The format is illegal and cannot be submitted");
    }
}

function register() {
    /** @namespace globalConfig.allowRegister */
    if (globalConfig.allowRegister) {
        var username = $("#username").val();
        var email = $("#email").val();
        var verifyCode = $("#email-verify-code").val();
        var password = $("#reg-password").val();
        var passwordConfirm = $("#confirm-password").val();
        var canRegister = username.match(userConfig.usernameMatch.pattern) && (!userConfig.emailVerify || 6 === verifyCode.length) && isEmail(email) && checkPassword(password, passwordConfirm);
        if (submit() && canRegister) {
            layer.load(1);
            $.post("/user/register", {
                username: username,
                email: email,
                password: password,
                code: verifyCode
            }, function (data) {
                layer.closeAll();
                var json = JSON.parse(data);
                if (json.status === "success") {
                    alerts("LOGIN WAS SUCCESSFUL");
                    switchToLogin();
                } else {
                    alerts(json.message);
                }
            });
        } else {
            alerts("Illegal content, unable to submit");
        }
    } else {
        alerts("This site has been banned from registration. Please contact the administrator");
    }
}

function login() {
    /** @namespace globalConfig.allowLogin */
    if (globalConfig.allowLogin) {
        var username = $("#loginName").val();
        var password = $("#password").val();
        var remember = document.getElementById("remember").checked;
        if (username && password) {
            layer.load(1);
            $.ajax({
                url: "/user/login", type: "PUT", data: {
                    username: username,
                    password: password,
                    auto: remember,
                    token: getCookie("token")
                }, success: function (data) {
                    layer.closeAll();
                    var json = JSON.parse(data);
                    if (json.status === "success") {
                        if (remember) {
                            var exp = new Date();
                            document.cookie = "token=" + json.token + ";expires=" + exp.setTime(exp.getTime() + 30 * 24 * 60 * 60 * 1000);
                        }
                        window.location.href = "/index";
                    } else {
                        alerts("Login failed, username or password incorrect");
                    }
                }
            });
        } else {
            alerts("User name or password cannot be empty");
        }
    } else {
        alerts("This site has been banned from logging in. Please contact the administrator");
    }
}

function switchToRegister() {
    switchTo("none", "none", "block", "register", signinItem.emailVerify ? 30 : 26);
}

function switchToLogin() {
    switchTo("block", "none", "none", "login", 24);
}

function switchToReset() {
    switchTo("none", "block", "none", "reset", 26);
}

function switchTo(login, reset, register, hash, height) {
    $("#login-div").css("display", login);
    $("#reset-div").css("display", reset);
    $("#register-div").css("display", register);
    window.location.hash = "#" + hash;
    $(".center-vertical").css("height", height + "rem");
    signinItem.description = "";
    signinItem.emailErrorTip = "";
    signinItem.emailVerifyStatus = "";
    signinItem.passwordVerify = "";
    signinItem.passwordConfirm = "";
}

function submit() {
    return isEmpty(signinItem.description) && isEmpty(signinItem.emailErrorTip) && isEmpty(signinItem.emailVerifyStatus) && isEmpty(signinItem.passwordVerify) && isEmpty(signinItem.passwordConfirm);
}

$(document).ready(
    function () {
        $("#username").keyup(function () {
                var username = event.srcElement.value;
                if (username.match(userConfig.usernameMatch.pattern)) {
                    $.get("/user/username/exists", {username: username}, function (data) {
                        var json = JSON.parse(data);
                        /** @namespace json.exists */
                        signinItem.description = json.exists ? "The username already exists" : "";
                    });
                } else {
                    signinItem.description = userConfig.usernameMatch.description;
                }
            }
        );
        $(".email").keyup(function () {
            var email = event.srcElement.value;
            if (isEmail(email)) {
                if (location.hash === "#register") {
                    $.get("/user/email/exists", {email: email}, function (data) {
                        var json = JSON.parse(data);
                        signinItem.emailErrorTip = json.exists ? "This email has already been registered" : "";
                    });
                }
                signinItem.emailErrorTip = "";
            } else {
                signinItem.emailErrorTip = "Incorrect email format";
            }
        });
        $(".password").keyup(function () {
            var len = event.srcElement.value.length;
            if (len >= userConfig.password.minLength && len <= userConfig.password.maxLength) {
                signinItem.passwordVerify = "";
            } else {
                signinItem.passwordVerify = "Password length is limited" + userConfig.password.minLength + "to" + userConfig.password.maxLength;
            }
        });
        $(".confirm-password").keyup(function () {
            var ele = event.srcElement;
            signinItem.passwordConfirm = (ele.value === $(ele).siblings(".password").val()) ? "" : "The passwords entered twice are different";
        });
        $(".sendVerifyCode").click(function () {
            var eventSrc = event.srcElement;
            console.info("test");
            sendVerifyCode($(eventSrc).parents(location.hash + "-div").find(".email").val(), eventSrc);
        });
        $(".email-verify-code").keyup(function () {
            var code = event.srcElement.value;
            if (code.length === 6) {
                $.ajax({
                    url: "/common/" + code + "/verification", type: "PUT", success: function (data) {
                        var json = JSON.parse(data);
                        signinItem.emailVerifyStatus = json.status === "success" ? "" : "Verification code error";
                    }
                });
            } else {
                signinItem.emailVerifyStatus = "";
            }
        });
    }
);

switch (window.location.hash) {
    case "#register":
        switchToRegister();
        break;
    case "#reset":
        switchToReset();
        break;
    default:
        switchToLogin();
        break;
}

layer.load(1);
$.get("/config/user", function (data) {
    layer.closeAll();
    userConfig = JSON.parse(data);
    signinItem.emailVerify = userConfig.emailVerify;
    /** @namespace userConfig.usernameMatch */
    if (window.location.hash === "#register") {
        switchToRegister();
    }
});