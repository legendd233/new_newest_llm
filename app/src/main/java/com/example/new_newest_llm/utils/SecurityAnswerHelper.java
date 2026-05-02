package com.example.new_newest_llm.utils;

public class SecurityAnswerHelper {

    /**
     * 归一化密保答案：去空白 → 转小写 → 中英文映射到统一值。
     * "猫"/"cats"/"cat"  → "cat"
     * "狗"/"dogs"/"dog"  → "dog"
     */
    public static String normalize(String raw) {
        if (raw == null) return "";
        String s = raw.strip().toLowerCase();
        switch (s) {
            case "猫":
            case "cats":
                return "cat";
            case "狗":
            case "dogs":
                return "dog";
            default:
                return s;
        }
    }
}
