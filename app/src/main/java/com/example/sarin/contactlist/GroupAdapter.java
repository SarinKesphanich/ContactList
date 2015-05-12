package com.example.sarin.contactlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;

    /**
     * Created by Sarin on 13/5/2558.
     */
    public class GroupAdapter extends ArrayAdapter<Groups> {
        ArrayList<Groups> groupList;
    LayoutInflater vi;
    int Resource;
    ViewHolder holder;

    public GroupAdapter(Context context, int resource, ArrayList<Groups> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resource = resource;
        groupList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // convert view = design
        View v = convertView;
        if (v == null) {
            holder = new ViewHolder();
            v = vi.inflate(Resource, null);
            holder.groupId = (TextView) v.findViewById(R.id.groupId);
            holder.groupName = (TextView) v.findViewById(R.id.groupName);
            holder.groupAmount = (TextView) v.findViewById(R.id.groupAmount);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        holder.groupId.setText(String.valueOf(groupList.get(position).getId()));
        holder.groupName.setText(groupList.get(position).getName());
        holder.groupAmount.setText(String.valueOf(groupList.get(position).getAmount()));
        return v;
    }


    static class ViewHolder {
        public TextView groupName, groupAmount, groupId;

    }
}
