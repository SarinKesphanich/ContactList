package com.example.sarin.contactlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Sarin on 14/5/2558.
 */
public class ContactAdapter extends ArrayAdapter<Contacts>{
    ArrayList<Contacts> contactList;
    LayoutInflater vi;
    int Resource;
    ViewHolder holder;

    public ContactAdapter(Context context, int resource, ArrayList<Contacts> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resource = resource;
        contactList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // convert view = design
        View v = convertView;
        if (v == null) {
            holder = new ViewHolder();
            v = vi.inflate(Resource, null);
            holder.contactId = (TextView) v.findViewById(R.id.contactId);
            holder.contactName = (TextView) v.findViewById(R.id.telNumber);
            holder.contactImg = (ImageView) v.findViewById(R.id.ivImage);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        holder.contactImg.setImageResource(R.mipmap.ic_launcher);
        new DownloadImageTask(holder.contactImg).execute(contactList.get(position).getImg());

        holder.contactId.setText(String.valueOf(contactList.get(position).getId()));
        holder.contactName.setText(contactList.get(position).getName());
        return v;
    }


    static class ViewHolder {
        public TextView contactName, contactId;
        public ImageView contactImg;

    }
}
