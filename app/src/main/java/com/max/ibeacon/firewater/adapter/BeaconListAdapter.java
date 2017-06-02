package com.max.ibeacon.firewater.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.max.ibeacon.firewater.R;

import org.altbeacon.beacon.Beacon;

import java.util.List;

/**
 * Created by max on 22/02/15.
 */
public class BeaconListAdapter extends ArrayAdapter<Beacon> {

        private Context context;

        public BeaconListAdapter(Context context, List<Beacon> items) {
            super(context, R.layout.beacon_list_item, items);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder = null;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.beacon_list_item, null);

                viewHolder = new ViewHolder();
                viewHolder.description = (TextView) convertView.findViewById(R.id.beaconDetail);
                viewHolder.image = (ImageView) convertView.findViewById(R.id.beaconImage);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final Beacon beacon = getItem(position);

            //setting description
            viewHolder.description.setText(String.format("UUID: %s\nDistance: %.2fm",
                    beacon.getId1()+"-"+beacon.getId2()+"-"+beacon.getId3(),
                    Beacon.getDistanceCalculator().calculateDistance(beacon.getTxPower(), beacon.getRssi())));
            viewHolder.image.setImageResource(R.drawable.beacon);

            return convertView;
        }

        //this is better approach as suggested by Google-IO for ListView
        private static class ViewHolder{
            TextView description;
            ImageView image;
        }

        public void refill(List<Beacon> items) {
            this.clear();
            this.addAll(items);
            this.notifyDataSetChanged();
        }
    }
