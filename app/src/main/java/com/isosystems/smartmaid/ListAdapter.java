package com.isosystems.smartmaid;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ListAdapter extends BaseAdapter {

    List<Room> rooms;
    private Context mContext;

    public ListAdapter(List<Room> l, Context c) {
        mContext = c;
        rooms = l;
    }

    public int getCount() {
        return rooms.size();
    }


    private int recalculateItemCount() {
        int result = 0;
        for (Room r : rooms) {
            if (r.dnd || r.maid || r.clean || r.wash || r.minibar) {
                result++;
            }
        }

        return result;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(this.mContext);
            v = vi.inflate(R.layout.list_item, null);
        }

//        LayoutInflater vi;
//        vi = LayoutInflater.from(this.mContext);
//        View v = vi.inflate(R.layout.list_item, null);

        //v.setVisibility(View.VISIBLE);

        TextView room_text = (TextView) v.findViewById(R.id.room_text);
        room_text.setText(String.valueOf(rooms.get(position).room_number));

        View dnd = (View) v.findViewById(R.id.dnd_indicator);
        View maid = (View) v.findViewById(R.id.maid_indicator);
        View clean = (View) v.findViewById(R.id.clean_indicator);
        View wash = (View) v.findViewById(R.id.wash_indicator);
        View minibar = (View) v.findViewById(R.id.minibar_indicator);

        if (!((MyApplication) mContext.getApplicationContext()).isRoomHiding) {
            if (rooms.get(position).valueChanged) {
                ObjectAnimator colorFade = ObjectAnimator.ofObject(v, "backgroundColor", new ArgbEvaluator(), Color.CYAN, Color.parseColor("#1452d4c4"));
                colorFade.setDuration(3000);
                colorFade.start();
                rooms.get(position).valueChanged = false;
            } else {
                //v.clearAnimation();
            }
        } else {
            if (rooms.get(position).valueChanged) rooms.get(position).valueChanged = false;
        }

        if (rooms.get(position).dnd) {
            dnd.setBackgroundColor(Color.RED);
        } else {
            dnd.setBackgroundColor(Color.TRANSPARENT);
        }

        if (rooms.get(position).maid) {
            maid.setBackgroundColor(Color.BLUE);
        } else {
            maid.setBackgroundColor(Color.TRANSPARENT);
        }

        if (rooms.get(position).clean) {
            clean.setBackgroundColor(Color.GREEN);
        } else {
            clean.setBackgroundColor(Color.TRANSPARENT);
        }

        if (rooms.get(position).wash) {
            wash.setBackgroundColor(Color.YELLOW);
        } else {
            wash.setBackgroundColor(Color.TRANSPARENT);
        }

        if (rooms.get(position).minibar) {
            minibar.setBackgroundColor(Color.MAGENTA);
        } else {
            minibar.setBackgroundColor(Color.TRANSPARENT);
        }

        return v;
    }

}