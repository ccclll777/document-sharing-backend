package org.ccclll777.alldocsbackend.utils;

public class RegexConstant {
    private RegexConstant() {
        throw new IllegalStateException("RegxConstant class error!");
    }

    public static final String CH_ENG_WORD = "^[\\u4E00-\\u9FA5A-Za-z0-9_-]{1,64}$";
}
