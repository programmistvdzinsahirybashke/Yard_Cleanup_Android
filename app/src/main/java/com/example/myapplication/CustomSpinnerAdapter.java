package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {

    private List<String> data;
    private LayoutInflater inflater;

    public CustomSpinnerAdapter(Context context, int resource, List<String> data) {
        super(context, resource, data);
        this.data = data;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(data.get(position));
        textView.setSingleLine(false); // Позволяет переносить текст на следующую строку

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(data.get(position));
        textView.setSingleLine(false); // Позволяет переносить текст на следующую строку

        return convertView;
    }
}
