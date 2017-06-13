package lrb.com.wuziqi;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by FengChaoQun
 * on 2017/5/4
 * 设置的工具类
 */

public class SettingUtils {

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private static final String FILE_NAME = "settings";

    private static final String WHITE_FIRST = "WHITE_FIRST";
    private static final String CHESSBOARD_SIZE = "CHESSBOARD_SIZE";

    public SettingUtils(Context context) {
        sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public void setFirstHand(boolean isWhiteFirst) {
        editor.putBoolean(WHITE_FIRST, isWhiteFirst);
        SharedPreferencesCompat.apply(editor);
    }

    public boolean isWhiteFirst() {
        return sp.getBoolean(WHITE_FIRST, true);
    }

    public void setChessBoardSize(int lines) {
        editor.putInt(CHESSBOARD_SIZE, lines);
        SharedPreferencesCompat.apply(editor);
    }

    public int getChessBoardSize() {
        return sp.getInt(CHESSBOARD_SIZE, 15);
    }

    /**
     * 创建一个解决SharedPreferencesCompat.apply方法的一个兼容类
     */
    private static class SharedPreferencesCompat {
        private static final Method sApplyMethod = findApplyMethod();

        /**
         * 反射查找apply的方法
         *
         * @return
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Method findApplyMethod() {
            try {
                Class clz = SharedPreferences.Editor.class;
                return clz.getMethod("apply");
            } catch (NoSuchMethodException e) {
            }

            return null;
        }

        /**
         * 如果找到则使用apply执行，否则使用commit
         *
         * @param editor
         */
        public static void apply(SharedPreferences.Editor editor) {
            try {
                if (sApplyMethod != null) {
                    sApplyMethod.invoke(editor);
                    return;
                }
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
            editor.commit();
        }
    }
}
