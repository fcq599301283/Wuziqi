package lrb.com.wuziqi.btGame;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by FengChaoQun
 * on 2017/5/5
 */

public class btAdapter extends ArrayAdapter<BluetoothDevice> {
    public btAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<BluetoothDevice> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view=View.inflate(getContext(),android.R.layout.simple_list_item_1,null);
        TextView textView= (TextView) view.findViewById(android.R.id.text1);
        textView.setText(""+getItem(position).getName());
        return view;
    }
}
