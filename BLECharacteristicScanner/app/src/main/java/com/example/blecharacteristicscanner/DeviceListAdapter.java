package com.example.blecharacteristicscanner;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DLViewHolder>
{
    private Context context;
    private List<Device> devices = new ArrayList<>();
    private OnItemClickListener listener;

    public DeviceListAdapter(Context context, List<Device> devices)
    {
        this.context = context;
        this.devices = devices;
    }

    public DeviceListAdapter(Context context)
    {
        this.context = context;
    }



    public void setOnItemClickListener(OnItemClickListener listener)
    {
        this.listener = listener;
    }

    public interface OnItemClickListener
    {
        void onItemClick(int position);
    }


    public class DLViewHolder extends RecyclerView.ViewHolder // This class it`s storage of item elements
    {
        TextView text_deviceName;
        TextView text_macAddress;
        ImageView image_rssiStrength;

        public DLViewHolder(View itemView)
        {
            super(itemView);
            text_deviceName = itemView.findViewById(R.id.device_name);
            text_macAddress = itemView.findViewById(R.id.device_mac);
            image_rssiStrength = itemView.findViewById(R.id.rssi_strength);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION)
                        listener.onItemClick(getAdapterPosition());
                }
            });
        }
    }

    @Override
    public DLViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_ble_device, parent, false);
        return new DLViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DLViewHolder holder, int position)
    {
        holder.text_deviceName.setText( devices.get(position).getName() );
        holder.text_macAddress.setText( devices.get(position).getMac() );
        holder.image_rssiStrength.setImageResource( rssiStrengthToPicId(devices.get(position).getStrength()) );
    }



    private int rssiStrengthToPicId(int strength)
    {
        if (strength > -45)
            return R.drawable.rssi_strength_4;
        if (strength > -62)
            return R.drawable.rssi_strength_3;
        if (strength > -80)
            return R.drawable.rssi_strength_2;
        if (strength > -95)
            return R.drawable.rssi_strength_1;
        else
            return R.drawable.rssi_strength_0;
    }

    @Override
    public int getItemCount() { return devices.size(); }

    public void addItem(Device item)
    {
        devices.add(item);
        notifyItemInserted(getItemCount() - 1); // обновляет только новый элемент
    }

    public int getItemIdByDeviceAddress(String address)
    {
        int result = -1;

        for (int i = 0; i < getItemCount(); i++)
        {
            if (devices.get(i).getMac().equals(address))
                return i;
        }

        return result;
    }

    public boolean setItemName(int id, String name)
    {
        if (id < getItemCount())
        {
            devices.get(id).setName(name);
            notifyItemChanged(id);
            return true;
        }
        else
            return false;
    }

    public boolean setItemStength(int id, int stength)
    {
        if (id < getItemCount())
        {
            devices.get(id).setStrength(stength);
            notifyItemChanged(id);
            return true;
        }
        else
            return false;
    }
}
