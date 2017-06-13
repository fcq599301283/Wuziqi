package lrb.com.wuziqi.utils;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FengChaoQun
 * on 2017/5/6
 * 电脑AI算法
 */

public class AIUtils {

    private static final int GAME_OVER = 10000;

    private static final int AILive1 = 4;
    private static final int HumanLive1 = 4;
    private static final int AISleep1 = 1;
    private static final int HumanSleep1 = 1;

    private static final int AILive2 = 25;
    private static final int HumanLive2 = 20;
    private static final int AISleep2 = 10;
    private static final int HumanSleep2 = 4;

    private static final int AILive3 = 450;
    private static final int HumanLive3 = 105;
    private static final int AISleep3 = 55;
    private static final int HumanSleep3 = 30;

    private static final int AILive4 = GAME_OVER;
    private static final int HumanLive4 = 2000;
    private static final int AISleep4 = GAME_OVER;
    private static final int HumanSleep4 = 2000;

    private int MAX_LINE;
    //记录人的落子
    private List<Point> humanPoints = new ArrayList<>();
    //记录电脑的落子
    private List<Point> AIPoints = new ArrayList<>();
    //记录每个位置的权值
    private int[][] value;

    public AIUtils(int MAX_LINE) {
        this.MAX_LINE = MAX_LINE;
        value = new int[MAX_LINE][MAX_LINE];
    }

    public Point getAIPoint(Point humanPoint) {
        //如果人没有落子 则是电脑先手 这时候选择在中间落子
        if (humanPoint == null) {
            return getAIPoint(MAX_LINE / 2, MAX_LINE / 2);
        }

        humanPoints.add(humanPoint);

        calculateValue();
        int maxValue = 0;
        int x = 0, y = 0;
        for (int i = 0; i < MAX_LINE; i++) {
            for (int j = 0; j < MAX_LINE; j++) {
                if (value[i][j] > maxValue) {
                    maxValue = value[i][j];
                    x = i;
                    y = j;
                }
            }
        }
        return getAIPoint(x, y);
    }

    private Point getAIPoint(int x, int y) {
        Point point = new Point(x, y);
        AIPoints.add(point);
        return point;
    }

    public void regret() {
        if (!humanPoints.isEmpty() && !AIPoints.isEmpty()) {
            humanPoints.remove(humanPoints.size() - 1);
            AIPoints.remove(AIPoints.size() - 1);
            calculateValue();
        }
    }

    private void calculateValue() {
        value = new int[MAX_LINE][MAX_LINE];
        //遍历整个棋盘
        for (int i = 0; i < MAX_LINE; i++) {
            for (int j = 0; j < MAX_LINE; j++) {
                Point point = new Point(i, j);
                //如果该位置已经有棋子了 就不用判断了
                if (humanPoints.contains(point) || AIPoints.contains(point)) {
                    value[i][j] = -1;
                    continue;
                }
                calculateHorizontal(point, true);
                calculateHorizontal(point, false);
                calculateVertical(point, true);
                calculateVertical(point, false);
                calculateLeftBias(point, true);
                calculateLeftBias(point, false);
                calculateRightBias(point, true);
                calculateRightBias(point, false);
            }
        }
    }

    //计算水平方向的权值
    private void calculateHorizontal(Point point, boolean AI) {
        int x = point.x;
        int y = point.y;
        int count = 0;
        boolean leftBoard = false, rightBoard = false;

        List<Point> parentPoints = AI ? AIPoints : humanPoints;
        List<Point> objectPoints = AI ? humanPoints : AIPoints;

        //计算左边的相同子数
        for (int i = 1; i <= 4; i++) {
            Point currentPoint = new Point(x - i, y);
            if (parentPoints.contains(currentPoint)) {
                count++;
            } else {
                if (currentPoint.x < 0 || objectPoints.contains(currentPoint)) {
                    leftBoard = true;
                }
                break;
            }
        }

        //计算右边的相同子数
        for (int i = 1; i <= 4; i++) {
            Point currentPoint = new Point(x + i, y);
            if (parentPoints.contains(currentPoint)) {
                count++;
            } else {
                if (currentPoint.x > MAX_LINE || objectPoints.contains(currentPoint)) {
                    rightBoard = true;
                }
                break;
            }
        }

        addValue(count, point, AI, leftBoard, rightBoard);

    }

    //计算垂直方向的权值
    private void calculateVertical(Point point, boolean AI) {
        int x = point.x;
        int y = point.y;
        int count = 0;
        boolean leftBoard = false, rightBoard = false;

        List<Point> parentPoints = AI ? AIPoints : humanPoints;
        List<Point> objectPoints = AI ? humanPoints : AIPoints;

        //计算上方的相同子数
        for (int i = 1; i <= 4; i++) {
            Point currentPoint = new Point(x, y - i);
            if (parentPoints.contains(currentPoint)) {
                count++;
            } else {
                //如果point的y<0 超出上边界
                if (currentPoint.y < 0 || objectPoints.contains(currentPoint)) {
                    leftBoard = true;
                }
                break;
            }
        }

        //计算下方的相同子数
        for (int i = 1; i <= 4; i++) {
            Point currentPoint = new Point(x, y + i);
            if (parentPoints.contains(currentPoint)) {
                count++;
            } else {
                //如果point的y>MAX_LINE 超出下边界
                if (currentPoint.y > MAX_LINE || objectPoints.contains(currentPoint)) {
                    rightBoard = true;
                }
                break;
            }
        }

        addValue(count, point, AI, leftBoard, rightBoard);

    }

    //计算左下斜对角线的权值
    private void calculateLeftBias(Point point, boolean AI) {
        int x = point.x;
        int y = point.y;
        int count = 0;
        boolean leftBoard = false, rightBoard = false;

        List<Point> parentPoints = AI ? AIPoints : humanPoints;
        List<Point> objectPoints = AI ? humanPoints : AIPoints;

        //计算左上方的相同子数
        for (int i = 1; i <= 4; i++) {
            Point currentPoint = new Point(x - i, y - i);
            if (parentPoints.contains(currentPoint)) {
                count++;
            } else {
                if (currentPoint.y < 0 || currentPoint.x < 0 || objectPoints.contains(currentPoint)) {
                    leftBoard = true;
                }
                break;
            }
        }

        //计算右下方的相同子数
        for (int i = 1; i <= 4; i++) {
            Point currentPoint = new Point(x + i, y + i);
            if (parentPoints.contains(currentPoint)) {
                count++;
            } else {
                if (currentPoint.y > MAX_LINE || currentPoint.x > MAX_LINE || objectPoints.contains(currentPoint)) {
                    rightBoard = true;
                }
                break;
            }
        }

        addValue(count, point, AI, leftBoard, rightBoard);

    }

    //计算右上斜对角线的权值
    private void calculateRightBias(Point point, boolean AI) {
        int x = point.x;
        int y = point.y;
        int count = 0;
        boolean leftBoard = false, rightBoard = false;

        List<Point> parentPoints = AI ? AIPoints : humanPoints;
        List<Point> objectPoints = AI ? humanPoints : AIPoints;

        //计算左下方的相同子数
        for (int i = 1; i <= 4; i++) {
            Point currentPoint = new Point(x - i, y + i);
            if (parentPoints.contains(currentPoint)) {
                count++;
            } else {
                if (currentPoint.y > MAX_LINE || currentPoint.x < 0 || objectPoints.contains(currentPoint)) {
                    leftBoard = true;
                }
                break;
            }
        }

        //计算右上方的相同子数
        for (int i = 1; i <= 4; i++) {
            Point currentPoint = new Point(x + i, y - i);
            if (parentPoints.contains(currentPoint)) {
                count++;
            } else {
                if (currentPoint.y < 0 || currentPoint.x > MAX_LINE || objectPoints.contains(currentPoint)) {
                    rightBoard = true;
                }
                break;
            }
        }

        addValue(count, point, AI, leftBoard, rightBoard);

    }

    private void addValue(int count, Point point, boolean AI, boolean leftBoard, boolean rightBoard) {
        if (leftBoard && rightBoard) {
            value[point.x][point.y] += 0;
            return;
        }

        switch (count) {
            case 0:
                value[point.x][point.y] += 0;
                break;
            case 1:
                if (leftBoard || rightBoard) {
                    if (AI) {
                        value[point.x][point.y] += AISleep1;
                    } else {
                        value[point.x][point.y] += HumanSleep1;
                    }
                } else {
                    if (AI) {
                        value[point.x][point.y] += AILive1;
                    } else {
                        value[point.x][point.y] += HumanLive1;
                    }
                }
                break;
            case 2:
                if (leftBoard || rightBoard) {
                    if (AI) {
                        value[point.x][point.y] += AISleep2;
                    } else {
                        value[point.x][point.y] += HumanSleep2;
                    }
                } else {
                    if (AI) {
                        value[point.x][point.y] += AILive2;
                    } else {
                        value[point.x][point.y] += HumanLive2;
                    }
                }
                break;
            case 3:
                if (leftBoard || rightBoard) {
                    if (AI) {
                        value[point.x][point.y] += AISleep3;
                    } else {
                        value[point.x][point.y] += HumanSleep3;
                    }
                } else {
                    if (AI) {
                        value[point.x][point.y] += AILive3;
                    } else {
                        value[point.x][point.y] += HumanLive3;
                    }
                }
                break;
            case 4:
                if (leftBoard || rightBoard) {
                    if (AI) {
                        value[point.x][point.y] += AISleep4;
                    } else {
                        value[point.x][point.y] += HumanSleep4;
                    }
                } else {
                    if (AI) {
                        value[point.x][point.y] += AILive4;
                    } else {
                        value[point.x][point.y] += HumanLive4;
                    }
                }
                break;
            default:
                value[point.x][point.y] += 0;
                break;
        }
    }

    public int[][] getValue() {
        return value;
    }

}
