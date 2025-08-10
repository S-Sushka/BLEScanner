package com.example.blecharacteristicscanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CharacteristicListAdapter extends RecyclerView.Adapter<com.example.blecharacteristicscanner.CharacteristicListAdapter.CViewHolder>
{
    private List<String> characteristicCodes;

    public CharacteristicListAdapter(List<String> characteristicCodes)
    {
        this.characteristicCodes = characteristicCodes;
    }

    public static class CViewHolder extends RecyclerView.ViewHolder // This class it`s storage of item elements
    {
        TextView text_characteristicCode;

        public CViewHolder(View itemView)
        {
            super(itemView);
            text_characteristicCode = itemView.findViewById(R.id.characteristic_code);
        }
    }

    @Override
    public com.example.blecharacteristicscanner.CharacteristicListAdapter.CViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_ble_characteristic, parent, false);
        return new com.example.blecharacteristicscanner.CharacteristicListAdapter.CViewHolder(view);
    }

    @Override
    public void onBindViewHolder(com.example.blecharacteristicscanner.CharacteristicListAdapter.CViewHolder holder, int position)
    {
        holder.text_characteristicCode.setText( characteristicCodes.get(position) );
    }

    @Override
    public int getItemCount() { return characteristicCodes.size(); }
}
