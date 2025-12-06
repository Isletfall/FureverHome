package cn.fzu.edu.furever_home.auth.service;

import cn.fzu.edu.furever_home.auth.entity.User;

public interface AuthService {
    User findByAccount(String account);

    boolean matches(String rawPassword, String passwordHash);

    void sendRegisterCode(String email);

    User registerWithEmail(String email, String code, String userName, String rawPassword);

    void sendResetCode(String email);

    void resetPassword(String email, String code, String newPassword);

    void sendLoginCode(String email);

    User loginWithEmailCode(String email, String code);
}
