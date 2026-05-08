package com.suprabha.neuracareapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

public class SosActivity extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_sos, container, false);
        Button btnSos = view.findViewById(R.id.btnSos);

        btnSos.setOnClickListener(v ->
                Toast.makeText(getActivity(), "SOS Alert Sent!", Toast.LENGTH_SHORT).show()
        );

        return view;
    }
}