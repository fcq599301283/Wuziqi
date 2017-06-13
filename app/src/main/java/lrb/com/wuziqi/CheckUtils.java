package lrb.com.wuziqi;

import android.graphics.Point;

import java.util.ArrayList;

/**
 * Created by FengChaoQun
 * on 2017/5/3
 * 检查游戏是否结束
 */

public class CheckUtils {

    private static final int FIVE = 5;

    public static boolean isWin(ArrayList<Point> points) {
        for (Point point : points) {
            int pointX = point.x;
            int pointY = point.y;
            if (checkHorizontal(pointX, pointY, points) ||
                    checkVertical(pointX, pointY, points) ||
                    checkLeftBias(pointX, pointY, points) ||
                    checkRightBias(pointX, pointY, points)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkHorizontal(int pointX, int pointY, ArrayList<Point> points) {
        int pieceCount = 1;
        for (int i = 1; i < 5; i++) {
            if (points.contains(new Point(pointX - i, pointY))) {
                pieceCount++;
            } else {
                break;
            }
        }
        return pieceCount == FIVE;
    }

    private static boolean checkVertical(int pointX, int pointY, ArrayList<Point> points) {
        int pieceCount = 1;
        for (int i = 1; i < 5; i++) {
            if (points.contains(new Point(pointX, pointY - i))) {
                pieceCount++;
            } else {
                break;
            }
        }
        return pieceCount == FIVE;
    }

    private static boolean checkLeftBias(int pointX, int pointY, ArrayList<Point> points) {
        int pieceCount = 1;
        for (int i = 1; i < 5; i++) {
            if (points.contains(new Point(pointX + i, pointY + i))) {
                pieceCount++;
            } else {
                break;
            }
        }
        return pieceCount == FIVE;
    }

    private static boolean checkRightBias(int pointX, int pointY, ArrayList<Point> points) {
        int pieceCount = 1;
        for (int i = 1; i < 5; i++) {
            if (points.contains(new Point(pointX + i, pointY - i))) {
                pieceCount++;
            } else {
                break;
            }
        }
        return pieceCount == FIVE;
    }

}
