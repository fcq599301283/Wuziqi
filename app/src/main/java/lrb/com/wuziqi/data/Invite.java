package lrb.com.wuziqi.data;

/**
 * Created by FengChaoQun
 * on 2017/5/5
 */

public class Invite {
    private String fromName;
    private boolean isWhiteFirst;
    private boolean objectPieceWhite;
    private int boardSize;

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public boolean isWhiteFirst() {
        return isWhiteFirst;
    }

    public void setWhiteFirst(boolean whiteFirst) {
        isWhiteFirst = whiteFirst;
    }

    public boolean isObjectPieceWhite() {
        return objectPieceWhite;
    }

    public void setObjectPieceWhite(boolean objectPieceWhite) {
        this.objectPieceWhite = objectPieceWhite;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }
}
