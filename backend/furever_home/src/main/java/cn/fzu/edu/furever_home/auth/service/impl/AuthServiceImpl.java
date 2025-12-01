package cn.fzu.edu.furever_home.auth.service.impl;

import cn.fzu.edu.furever_home.auth.entity.Role;
import cn.fzu.edu.furever_home.auth.entity.User;
import cn.fzu.edu.furever_home.auth.entity.UserRole;
import cn.fzu.edu.furever_home.auth.mapper.RoleMapper;
import cn.fzu.edu.furever_home.auth.mapper.UserMapper;
import cn.fzu.edu.furever_home.auth.mapper.UserRoleMapper;
import cn.fzu.edu.furever_home.auth.service.AuthService;
import cn.fzu.edu.furever_home.common.enums.Sex;
import cn.fzu.edu.furever_home.common.enums.UserStatus;
import cn.fzu.edu.furever_home.tools.mail.EmailSender;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static cn.dev33.satoken.SaManager.log;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final EmailSender emailSender;
    private final BCryptPasswordEncoder encoder;
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public User findByAccount(String account) {
        if (StringUtils.isBlank(account)) {
            return null;
        }
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<User>()
                .eq(User::getEmail, account)
                .or()
                .eq(User::getUserName, account);
        List<User> userList = userMapper.selectList(qw);
//        log.info(userList.toString());
        if (CollectionUtils.isEmpty(userList)) {
            return null;
        }
        return userList.get(0);
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        return encoder.matches(rawPassword, passwordHash);
    }

    @Override
    public void sendRegisterCode(String email) {
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, email)) > 0) {
            throw new IllegalStateException("邮箱已注册");
        }
        String code = String.valueOf(100000 + new Random().nextInt(900000));
        String key = "reg:email:" + email;
        stringRedisTemplate.opsForValue().set(key, code, Duration.ofMinutes(10));
        emailSender.sendVerificationCode(email, code, fromEmail);
    }

    @Override
    public User registerWithEmail(String email, String code, String userName, String rawPassword) {
        String key = "reg:email:" + email;
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached == null || !cached.equals(code)) {
            throw new IllegalStateException("验证码无效或已过期");
        }
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, email)) > 0) {
            throw new IllegalStateException("邮箱已被使用");
        }
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUserName, userName)) > 0) {
            throw new IllegalStateException("昵称已被使用");
        }
        User u = new User();
        u.setUserName(userName);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(rawPassword));
        u.setUserAge(18);
        u.setSex(Sex.valueOf("男"));
        u.setStatus(UserStatus.valueOf("正常"));
        //u.setSex(Sex.MALE);
        //u.setStatus(UserStatus.NORMAL);
        u.setCreateTime(LocalDateTime.now());
        u.setUpdatedAt(LocalDateTime.now());
        try {
            userMapper.insert(u);
        } catch (DuplicateKeyException e) {
            throw new IllegalStateException("邮箱或昵称已存在，注册失败");
        }

        Role userRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, "USER"));
        if (userRole != null) {
            UserRole ur = new UserRole();
            ur.setUserId(u.getUserId());
            ur.setRoleId(userRole.getRoleId());
            ur.setCreatedAt(LocalDateTime.now());
            userRoleMapper.insert(ur);
        }

        stringRedisTemplate.delete(key);
        return u;
    }

    @Override
    public void sendResetCode(String email) {
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (u == null) {
            throw new IllegalStateException("邮箱未注册");
        }
        String code = String.valueOf(100000 + new Random().nextInt(900000));
        String key = "reset:email:" + email;
        stringRedisTemplate.opsForValue().set(key, code, Duration.ofMinutes(10));
        emailSender.sendVerificationCode(email, code, null);
    }

    @Override
    public void resetPassword(String email, String code, String newPassword) {
        String key = "reset:email:" + email;
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached == null || !cached.equals(code)) {
            throw new IllegalStateException("验证码无效或已过期");
        }
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (u == null) {
            throw new IllegalStateException("邮箱未注册");
        }
        u.setPasswordHash(encoder.encode(newPassword));
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(u);
        stringRedisTemplate.delete(key);
    }
}