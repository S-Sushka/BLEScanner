package com.example.blecharacteristicscanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServiceListAdapter extends RecyclerView.Adapter<com.example.blecharacteristicscanner.ServiceListAdapter.SViewHolder>
{
    private Context context;
    private List<Service> services = new ArrayList<>();

    public ServiceListAdapter(Context context, List<Service> services)
    {
        this.context = context;
        this.services = services;
    }
    public ServiceListAdapter(Context context)
    {
        this.context = context;
    }

    public static class SViewHolder extends RecyclerView.ViewHolder // This class it`s storage of item elements
    {
        TextView code;
        RecyclerView characteristicViewer;

        public SViewHolder(View itemView)
        {
            super(itemView);
            code = itemView.findViewById(R.id.service_code);
            characteristicViewer = itemView.findViewById(R.id.characteristic_viewer);
        }
    }

    @Override
    public com.example.blecharacteristicscanner.ServiceListAdapter.SViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_ble_service, parent, false);
        return new com.example.blecharacteristicscanner.ServiceListAdapter.SViewHolder(view);
    }

    @Override
    public void onBindViewHolder(com.example.blecharacteristicscanner.ServiceListAdapter.SViewHolder holder, int position)
    {
        holder.code.setText(services.get(position).getCode());

        if (holder.characteristicViewer.getItemDecorationCount() == 0)
        {
            int spacingInPixels = (int) (8 * context.getResources().getDisplayMetrics().density);
            holder.characteristicViewer.addItemDecoration(new SpacingItemDecoration(spacingInPixels));

            holder.characteristicViewer.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
            holder.characteristicViewer.setAdapter( new CharacteristicListAdapter(services.get(position).getCharacteristics()) );
            holder.characteristicViewer.setHasFixedSize(true);
        }
    }


    public void addItem(Service item)
    {
        services.add(item);
        notifyItemInserted(services.size() - 1); // обновляет только новый элемент
    }

    public void clear()
    {
        services.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() { return services.size(); }
}
