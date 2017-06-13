package lrb.com.wuziqi;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import lrb.com.wuziqi.utils.ToastUtils;

/**
 * Created by FengChaoQun
 * on 2017/5/3
 * 棋盘
 */

public class ChessBoard extends View {

    private int MAX_LINE = 15;                                  //棋盘的行数
    private Paint paint = new Paint();                          //画笔
    private float chessBoardWitdth;                             //棋盘宽度
    private final static float pieceSizeRatio = 0.75f;          //棋子相对格子的大小
    private float gridSize;                                     //格子大小
    private Bitmap whitePice;                                   //白色棋子素材
    private Bitmap blackPiece;                                  //黑色棋子素材
    private boolean isWhiteTurn = true;                        //轮到谁下棋  默认白旗先手
    private ArrayList<Point> WhitePiecePoint = new ArrayList<>();   //存放白棋子坐标
    private ArrayList<Point> BlackPiecePoint = new ArrayList<>();   //存放黑棋子坐标

    private boolean IsGameOver = false;                         //游戏是否结束

    private static final int WHITE_WIN = 0, BLACK_WIN = 1, TIE = -1;

    private ValueAnimator animator;
    private float lineLengthRatio;
    private onGameOver gameOver;

    private boolean isNeedWait;                 //标志是否需要等待对方落子
    private boolean isMyTurn = true;
    private boolean isReady = true;             //标志对方是否准备好了  主要用在蓝牙对战

    private boolean drawValues;                 //标志是否需要绘制权值信息
    private int[][] value;                      //权值信息


    public ChessBoard(Context context) {
        super(context);
        init();
    }

    public ChessBoard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChessBoard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * description:初始化画笔和棋子
     */

    private void init() {
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.FILL);
        whitePice = BitmapFactory.decodeResource(getResources(), R.drawable.white_piece);
        blackPiece = BitmapFactory.decodeResource(getResources(), R.drawable.black_piece);
    }

    /**
     * description:重写onMeasure方法 解决在ScrollView中布局不显示问题
     */

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = Math.min(widthSize, heightSize);
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            width = heightSize;
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            width = widthSize;
        }

        setMeasuredDimension(width, width);
    }

    /**
     * description:当棋盘的大小确定过后，会调用此方法，在这里初始化棋盘大小
     */

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initSize(w);
    }

    private void initSize(int size) {
        chessBoardWitdth = size;
        gridSize = chessBoardWitdth * 1.0f / MAX_LINE;
        int realSize = (int) (gridSize * pieceSizeRatio);
        whitePice = Bitmap.createScaledBitmap(whitePice, realSize, realSize, false);
        blackPiece = Bitmap.createScaledBitmap(blackPiece, realSize, realSize, false);
    }

    //开始绘制
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLines(canvas);
        drawPieces(canvas);
        if (drawValues) {
            drawValues(canvas);
        }
    }

    /**
     * description:画线的时候，棋盘四周都会有一定的空隙  因为边界线上也是可以下棋的
     * 这个空隙的大小目前定为棋盘格子的1/
     *
     * @param canvas 画布
     */

    private void drawLines(Canvas canvas) {
        paint.setColor(Color.BLACK);
        for (int i = 0; i < MAX_LINE; i++) {
            float ratio = lineLengthRatio * (1 + (MAX_LINE - i + 1) * 1f / MAX_LINE) >= 1 ? 1f :
                    lineLengthRatio * (1 + (MAX_LINE - i - 1) * 1f / MAX_LINE);
            int startX = (int) gridSize / 2;
            int endX = (int) ((chessBoardWitdth - gridSize / 2) * ratio);
            int y = (int) ((0.5 + i) * gridSize);
            canvas.drawLine(startX, y, endX, y, paint);//画横线
            canvas.drawLine(y, startX, y, endX, paint);//画竖线
        }
    }

    /**
     * description:绘制棋子，棋子摆放在网格的交点上
     */

    private void drawPieces(Canvas canvas) {
        drawPieces(canvas, WhitePiecePoint, whitePice);
        drawPieces(canvas, BlackPiecePoint, blackPiece);
    }

    private void drawPieces(Canvas canvas, ArrayList<Point> pointArrayList, Bitmap piece) {
        for (int i = 0, n = pointArrayList.size(); i < n; i++) {
            Point blackPoint = pointArrayList.get(i);
            canvas.drawBitmap(piece,
                    (blackPoint.x + (1 - pieceSizeRatio) / 2) * gridSize,
                    (blackPoint.y + (1 - pieceSizeRatio) / 2) * gridSize, null);
        }
    }

    //绘制权值信息
    private void drawValues(Canvas canvas) {
        if (value == null) {
            return;
        }
        paint.setTextSize(20);
        paint.setTextAlign(Paint.Align.RIGHT);
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < value[i].length; j++) {
                int currentValue = value[i][j];
                if (currentValue <= 0) {
                    paint.setColor(Color.WHITE);
                } else if (currentValue <= 10) {
                    paint.setColor(Color.GREEN);
                } else if (currentValue < 25) {
                    paint.setColor(Color.BLUE);
                } else if (currentValue <= 105) {
                    paint.setColor(getResources().getColor(R.color.colorAccent));
                } else {
                    paint.setColor(Color.RED);
                }
                canvas.drawText(String.valueOf(value[i][j]),
                        (i + 0.5f) * gridSize - 3,
                        (j + 0.5f) * gridSize - 3,
                        paint);
            }
        }
    }

    //判断游戏是否正在进行
    public boolean isGamePlaying() {
        return !(IsGameOver || (WhitePiecePoint.isEmpty() && BlackPiecePoint.isEmpty()));
    }

    //判断游戏是否结束
    private void IsGameOver() {
        if (CheckUtils.isWin(WhitePiecePoint)) {
            IsGameOver = true;
            gameOver(WHITE_WIN);
        } else if (CheckUtils.isWin(BlackPiecePoint)) {
            IsGameOver = true;
            gameOver(BLACK_WIN);
        } else if (isTie()) {
            IsGameOver = true;
            gameOver(TIE);
        } else {
            IsGameOver = false;
        }
    }

    private void gameOver(int state) {
        if (gameOver == null) {
            return;
        }
        switch (state) {
            case WHITE_WIN:
                gameOver.onWhiteWin();
                break;
            case BLACK_WIN:
                gameOver.onBlackWin();
                break;
            case TIE:
                gameOver.onTie();
                break;
        }
    }

    private boolean isTie() {
        return WhitePiecePoint.size() + BlackPiecePoint.size() == MAX_LINE * MAX_LINE;
    }

    //悔棋
    public void regret() {
        if (BlackPiecePoint.size() > 0 || WhitePiecePoint.size() > 0) {
            if (isWhiteTurn) {
                BlackPiecePoint.remove(BlackPiecePoint.size() - 1);
                isWhiteTurn = !isWhiteTurn;
            } else {
                WhitePiecePoint.remove(WhitePiecePoint.size() - 1);
                isWhiteTurn = !isWhiteTurn;
            }
            IsGameOver();
            invalidate();

        } else {
            Toast.makeText(getContext(), "一个子也没下，你悔个毛线", Toast.LENGTH_SHORT).show();
        }
    }

    //悔棋 （人机对战时 其实收回的是两个棋子）
    public void regret2() {
        if (BlackPiecePoint.size() > 0 || WhitePiecePoint.size() > 0) {
            BlackPiecePoint.remove(BlackPiecePoint.size() - 1);
            WhitePiecePoint.remove(WhitePiecePoint.size() - 1);
            IsGameOver();
            invalidate();
            setReady(true);
        }
    }

    //重新开始
    public void restart() {
        WhitePiecePoint.clear();
        BlackPiecePoint.clear();
        IsGameOver = false;
        value = null;
        invalidate();
        startAnim();
    }

    //重新绘制界面
    public void rebuild() {
        initSize((int) chessBoardWitdth);
        restart();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnim();
    }

    private void startAnim() {
        animator = ValueAnimator.ofFloat(0, 1f);
        animator.setDuration(1500);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                lineLengthRatio = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();
    }

    //监听触摸事件  当用户点击棋盘时  做出反应
    public boolean onTouchEvent(MotionEvent event) {
        if (IsGameOver) {
            Log.d("ChessBoard", "the game is over");
            return false;
        }
        if (isNeedWait && !isMyTurn) {
            Log.d("ChessBoard", "not your turn");
            return false;
        }

        if (isNeedWait && !isReady) {
            ToastUtils.showShort(getContext(), "对手还没有准备好");
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            Point p = getLegalPoint(x, y);
            if (BlackPiecePoint.contains(p) || WhitePiecePoint.contains(p)) {
                return false;
            }

            if (isWhiteTurn) {
                WhitePiecePoint.add(p);
            } else {
                BlackPiecePoint.add(p);
            }
            invalidate();
            isWhiteTurn = !isWhiteTurn;

            //每次落子都要检测游戏是否结束
            IsGameOver();

            if (isNeedWait) {
                isMyTurn = false;
                if (onPlay != null) {
                    onPlay.onPlay(p);
                }
            }
            return true;
        }
        return true;
    }

    private Point getLegalPoint(int x, int y) {
        return new Point((int) (x / gridSize), (int) (y / gridSize));
    }

    public void setGameOver(onGameOver gameOver) {
        this.gameOver = gameOver;
    }

    public interface onGameOver {
        void onWhiteWin();

        void onBlackWin();

        void onTie();
    }

    public void setMAX_LINE(int MAX_LINE) {
        this.MAX_LINE = MAX_LINE;
    }

    public void setWhiteTurn(boolean whiteTurn) {
        isWhiteTurn = whiteTurn;
    }

    public void setMyTurn(boolean myTurn) {
        isMyTurn = myTurn;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public boolean isMyTurn() {
        return isMyTurn;
    }

    public void setNeedWait(boolean needWait) {
        isNeedWait = needWait;
    }

    public void addPoint(Point point) {
        if (isWhiteTurn) {
            WhitePiecePoint.add(point);
        } else {
            BlackPiecePoint.add(point);
        }
        isMyTurn = !isMyTurn;
        isWhiteTurn = !isWhiteTurn;
        IsGameOver();
        invalidate();
    }

    private OnPlay onPlay;

    public void setOnPlay(OnPlay onPlay) {
        this.onPlay = onPlay;
    }

    public interface OnPlay {
        void onPlay(Point point);

    }

    public ArrayList<Point> getWhitePiecePoint() {
        return WhitePiecePoint;
    }

    public ArrayList<Point> getBlackPiecePoint() {
        return BlackPiecePoint;
    }

    public void setValue(int[][] value) {
        this.value = value;
        Log.d("ChessBoard", "value:" + Arrays.deepToString(value));
    }

    public void setDrawValues(boolean drawValues) {
        this.drawValues = drawValues;
    }
}
