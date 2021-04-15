package com.example.klasyfikatorptakow;

import android.content.Context;
import android.content.SharedPreferences;

import android.graphics.Bitmap;

import android.os.Bundle;

import android.telephony.SmsManager;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.camera.core.CameraSelector;

import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class CameraFragment extends Fragment {

    SharedPreferences sharedPref;
    SharedPreferences sharedPref2;
    public static int golab = 0;
    public static int kos = 0;
    public static int kruk = 0;
    public static int sroka = 0;
    public DatabaseReference dataBase;
    public String deviceName;
    public String phoneNumber;

    private Context context;
    int prediction = 4;
    public Thread timer;
    Interpreter interpreter;
    Bitmap bitmap;
    String labels[] = {"golab","kos","kruk","sroka"};
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    static int indexOfLargest(float[] array)
    {
        int max = 0;
        for (int i = 1; i < array.length; i++)
            if (array[i] > array[max])
                max = i;
        return max;
    }

    public void refreshDataBase(float[] predictions)
    {
        if(dataBase != null) {
            dataBase.child(deviceName).child("golab").setValue(golab);
            dataBase.child(deviceName).child("kos").setValue(kos);
            dataBase.child(deviceName).child("kruk").setValue(kruk);
            dataBase.child(deviceName).child("sroka").setValue(sroka);
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            int prediction = indexOfLargest(predictions);
            dataBase.child(deviceName).child("wszystkie predykcje").child(dateFormat.format(date)).setValue(labels[prediction]+" "+String.format("%.2f", predictions[prediction]*100)+"%");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }


    public CameraFragment ()
    {
    }

    @Override
    public void onPause() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("golab", golab);
        editor.putInt("kos", kos);
        editor.putInt("kruk", kruk);
        editor.putInt("sroka", sroka);
        editor.apply();
        timer.interrupt();
        super.onPause();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.camerafragment, container, false);

        dataBase = FirebaseDatabase.getInstance("https://fotopulapki-52374-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        golab = sharedPref.getInt("golab",0);
        kos = sharedPref.getInt("kos",0);
        kruk = sharedPref.getInt("kruk",0);
        sroka = sharedPref.getInt("sroka",0);

        sharedPref2 = getActivity().getSharedPreferences("klasyfikator_ptakow_preferencje",Context.MODE_PRIVATE);
        deviceName = sharedPref2.getString("deviceName","");
        phoneNumber = sharedPref2.getString("phoneNumber","");

        PreviewView previewView = rootView.findViewById(R.id.previewView);

        ListenableFuture cameraProviderFuture =
                ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                // Camera provider is now guaranteed to be available
                ProcessCameraProvider cameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();

                // Set up the view finder use case to display camera preview
                Preview preview = new Preview.Builder().build();


                // Choose the camera by requiring a lens facing
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                // Attach use cases to the camera with the same lifecycle owner
                cameraProvider.bindToLifecycle(
                        ((LifecycleOwner) this),
                        cameraSelector,
                        preview);

                // Connect the preview use case to the previewView
                preview.setSurfaceProvider(
                        previewView.getSurfaceProvider());
            } catch (InterruptedException | ExecutionException e) {
                // Currently no exceptions thrown. cameraProviderFuture.get() should
                // not block since the listener is being called, so no need to
                // handle InterruptedException.
            }
        }, ContextCompat.getMainExecutor(context));

        timer = new Thread(){
            @NonNull
            @Override
            public void run(){
                while(!isInterrupted()){
                    try {
                        Thread.sleep(1500);
                        if(getActivity() == null) continue;
                        getActivity().runOnUiThread(new Runnable(){
                            @Override
                            public void run(){
                                bitmap = previewView.getBitmap();
                            }
                        });
                        Thread.sleep(500);
                        if(bitmap != null) {
                            TensorBuffer output = TensorBuffer.createFixedSize(new int[]{1, 4}, DataType.FLOAT32); // 4 wyniki
                            bitmap = Bitmap.createScaledBitmap(bitmap,224,224, false);
                            int[] pixels = new int[224*224];
                            bitmap.getPixels(pixels,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
                            ByteBuffer pixelRGBFloat32 = ByteBuffer.allocateDirect(4*224*224*3);
                            pixelRGBFloat32.order(ByteOrder.nativeOrder());
                            for(int i = 0;i<224*224;i++) {
                                pixelRGBFloat32.putFloat(((pixels[i]>> 16) & 0xFF) / 255.f);
                                pixelRGBFloat32.putFloat(((pixels[i]>> 8) & 0xFF) / 255.f);
                                pixelRGBFloat32.putFloat((pixels[i] & 0xFF) / 255.f);
                            }
                            try {
                                MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(context, "model.tflite");
                                interpreter = new Interpreter(tfliteModel, new Interpreter.Options());
                            } catch (Exception e) {
                            }
                            if (interpreter != null) {
                                interpreter.run(pixelRGBFloat32, output.getBuffer());
                            }
                            float predictions[] = output.getFloatArray();
                            if (prediction != indexOfLargest(predictions) && predictions[indexOfLargest(predictions)] >= 0.5) {
                                if(getActivity() != null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(context, labels[indexOfLargest(predictions)], Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                if (indexOfLargest(predictions) == 0) golab++;
                                if (indexOfLargest(predictions) == 1) kos++;
                                if (indexOfLargest(predictions) == 2) kruk++;
                                if (indexOfLargest(predictions) == 3) sroka++;
                                prediction = indexOfLargest(predictions);
                                refreshDataBase(predictions);
                                if(phoneNumber != null) {
                                    try{
                                        String message = deviceName + " wykryl: " + labels[prediction]+" "+String.format("%.2f", predictions[prediction]*100)+"%";
                                        SmsManager.getDefault().sendTextMessage(phoneNumber, null,message,null,null);
                                    }
                                    catch(Exception e){
                                        if(getActivity() != null) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                }
                            } else if (predictions[indexOfLargest(predictions)] < 0.5)
                                prediction = 4;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        };
        timer.start();
        return rootView;
    }

}


