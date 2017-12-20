package com.xat.barcodestore;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.xat.barcodestore.model.Barcode;

import java.util.Arrays;
import java.util.List;

public class BarcodeArrayAdapter extends ArrayAdapter<Barcode> {
    private Context context;
    private List<Barcode> barcodes;

    public BarcodeArrayAdapter(Context context, Barcode[] values) {
        super(context, -1, values);
        this.context = context;
        this.barcodes = Arrays.asList(values);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.barcode_row, parent, false);
        TextView nameView = rowView.findViewById(R.id.name);
        nameView.setText(barcodes.get(position).getName());

        TextView valueView = rowView.findViewById(R.id.value);
        valueView.setText(barcodes.get(position).getValue());

        return rowView;
    }
}
