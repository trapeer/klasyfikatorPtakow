package com.example.klasyfikatorptakow;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;



public class ResultsFragment extends Fragment {

    private Context context;
    TextView textViewResults;
    Button buttonReset;
    Thread timer;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public ResultsFragment () {

    }

    @Override
    public void onPause() {
        timer.interrupt();
        super.onPause();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.resultsfragment, container, false);
        textViewResults = rootView.findViewById(R.id.textViewResults);
        buttonReset = rootView.findViewById(R.id.buttonReset);
        refresh();

        timer = new Thread(){
            @Override
            public void run() {
                while (!isInterrupted()) {
                    try {
                        Thread.sleep(1000);
                        if(getActivity() == null) continue;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refresh();
                            }
                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        timer.start();

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraFragment.golab = 0;
                CameraFragment.kos = 0;
                CameraFragment.kruk = 0;
                CameraFragment.sroka = 0;
                refresh();
            }
        });
        return rootView;
    }

    public void refresh()
    {
        textViewResults.setText("golab: " + CameraFragment.golab + "\nkos: " + CameraFragment.kos + "\nkruk: " + CameraFragment.kruk + "\nsroka: " + CameraFragment.sroka);
    }

}
