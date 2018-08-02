package org.lockiely.web.controller;

import java.util.Date;
import javax.validation.Valid;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.lockiely.exception.BusinessException;
import org.lockiely.persistence.entity.User;
import org.lockiely.persistence.entity.enums.ActiveEnum;
import org.lockiely.persistence.entity.enums.StatusEnum;
import org.lockiely.persistence.mapper.UserMapper;
import org.lockiely.shiro.ShiroLocalContextHolder;
import org.lockiely.shiro.ShiroUser;
import org.lockiely.shiro.ShiroUtils;
import org.lockiely.utils.HttpRequestUtils;
import org.lockiely.utils.RequestResponseHolder;
import org.lockiely.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author: lockiely
 * @Date: 2018/7/24 19:27
 * @email: lockiely@163.com
 */
@Controller
public class UserController extends BaseController{

    @Autowired
    private UserMapper userMapper;

    /**
     * 点击登录执行的动作
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public Object login(@RequestBody User user) {
        Subject currentUser = ShiroUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(user.getAccount(), user.getPassword().toCharArray());
        token.setRememberMe(true);

        currentUser.login(token);

//        ShiroUser shiroUser = ShiroUtils.getUser();
//        HttpRequestUtils.getRequest().getSession().setAttribute("shiroUser", shiroUser);
//        HttpRequestUtils.getRequest().getSession().setAttribute("username", shiroUser.getAccount());
//
//        HttpRequestUtils.getRequest().getSession().setAttribute("sessionFlag",true);

        return "success";
    }

    /**
     * 退出登录
     * @return 跳转到登录
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    @ResponseBody
    public String logout(){
        Subject currentUser = ShiroUtils.getSubject();
        currentUser.logout();
        return REDIRECT + "/login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    @ResponseBody
    public String login(){
        if(ShiroUtils.isAuthenticated() || ShiroUtils.getUser() != null){
            return REDIRECT + "/";
        }else{
            String failure = "请登录";
            return failure;
        }
    }

    /**
     * 添加管理员
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String add(@RequestBody User user) {
        // 判断账号是否重复
        User theUser = userMapper.getByAccount(user.getAccount());
        if (theUser != null && StringUtils.hasText(theUser.getAccount())) {
            throw new BusinessException("当前账号已存在");
        }

        // 完善账号信息
        user.setSalt(ShiroUtils.getSalt());
        user.setPassword(ShiroUtils.getCredentials(user));
        user.setStatus((Integer) StatusEnum.CREATE.getValue());
        user.setCreateTime(new Date());
        user.setActive(ActiveEnum.ENABLE);
        userMapper.insert(user);
        return "success";
    }

}
