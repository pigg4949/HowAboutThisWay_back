package com.HATW.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    public static String encode(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}