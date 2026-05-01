package com.example.new_newest_llm.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import java.util.Locale;

/**
 * 管理应用语言偏好：中文 / English 二选一，默认中文。
 */
public class LocaleHelper {

    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_LANGUAGE = "pref_language";

    public static final String LANG_CHINESE = "zh";
    public static final String LANG_ENGLISH = "en";

    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, LANG_CHINESE);
    }

    public static void setLanguage(Context context, String language) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, language).apply();
    }

    /** 根据当前配置解析实际语言 */
    private static Locale resolveLocale(Context context) {
        String lang = getLanguage(context);
        if (LANG_ENGLISH.equals(lang)) return Locale.ENGLISH;
        return Locale.SIMPLIFIED_CHINESE;
    }

    public static boolean isChinese(Context context) {
        return LANG_CHINESE.equals(getLanguage(context));
    }

    /** 切换语言并返回新语言 */
    public static String toggleLanguage(Context context) {
        String newLang = isChinese(context) ? LANG_ENGLISH : LANG_CHINESE;
        setLanguage(context, newLang);
        return newLang;
    }

    /** 应用当前语言到 AppCompat（切换语言后调用） */
    public static void applyLocale(Context context) {
        Locale locale = resolveLocale(context);
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale));
    }

    /**
     * 为 Activity 包装 Context，确保该 Activity 的资源使用正确 locale。
     * 在 Activity.attachBaseContext() 中调用。
     */
    public static ContextWrapper wrapContext(Context base) {
        Locale locale = resolveLocale(base);

        Resources resources = base.getResources();
        Configuration config = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            return new ContextWrapper(base.createConfigurationContext(config));
        } else {
            @SuppressWarnings("deprecation")
            Configuration oldConfig = new Configuration(config);
            oldConfig.setLocale(locale);
            return new ContextWrapper(base.createConfigurationContext(oldConfig));
        }
    }

    /** 获取按钮显示文字：显示可切换到的语言 */
    public static String getToggleLabel(Context context) {
        return isChinese(context) ? "EN" : "中";
    }
}
