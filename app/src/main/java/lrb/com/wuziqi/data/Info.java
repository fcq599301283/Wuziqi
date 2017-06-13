package lrb.com.wuziqi.data;

/**
 * Created by FengChaoQun
 * on 2017/5/5
 */

public class Info {

    public static final int TYPE_INVITE = 0;
    public static final int TYPE_REFUSE = 1;
    public static final int TYPE_AGREE = 2;
    public static final int PLAY = 3;
    public static final int READY = 4;

    private int type;
    private String info;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
