package com.example.new_newest_llm.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import java.util.Locale;

/**
 * 管理应用语言偏好：跟随系统 / 中文 / English 三选一。
 * 首启默认跟随系统 locale。
 */
public class LocaleHelper {

    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_LANGUAGE = "pref_language";

    public static final String LANG_SYSTEM = "system";
    public static final String LANG_CHINESE = "zh";
    public static final String LANG_ENGLISH = "en";

    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, LANG_SYSTEM);
    }

    public static void setLanguage(Context context, String language) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, language).apply();
    }

    /** 判断当前有效语言是否为中文 */
    public static boolean isChinese(Context context) {
        String lang = getLanguage(context);
        if (LANG_CHINESE.equals(lang)) return true;
        if (LANG_ENGLISH.equals(lang)) return false;
        // 跟随系统
        return Locale.getDefault().getLanguage().equals(Locale.CHINESE.getLanguage());
    }

    /** 在 Application 或启动 Activity 中调用，覆盖系统 locale */
    public static void applyLocale(Context context) {
        String lang = getLanguage(context);
        Locale locale = null;
        if (LANG_CHINESE.equals(lang)) {
            locale = Locale.SIMPLIFIED_CHINESE;
        } else if (LANG_ENGLISH.equals(lang)) {
            locale = Locale.ENGLISH;
        }
        // system → 不覆盖，由资源系统自动跟随
        if (locale != null) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale));
        }
    }

    /** 获取当前语言偏好的显示名称 */
    public static String getLanguageLabel(Context context) {
        String lang = getLanguage(context);
        switch (lang) {
            case LANG_CHINESE:  return "中文";
            case LANG_ENGLISH:  return "English";
            default:            return isChinese(context) ? "中文" : "English";
        }
    }
}
