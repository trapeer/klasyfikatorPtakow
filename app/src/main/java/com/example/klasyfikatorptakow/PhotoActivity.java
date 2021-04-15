package com.example.klasyfikatorptakow;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.QuantizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.util.concurrent.ExecutionException;

public class PhotoActivity extends AppCompatActivity {
    TextView textViewPrediction;
    Button buttonMakeFoto;
    Interpreter interpreter;
    String labels[] = {"golab","kos","kruk","sroka"};


    static int indexOfLargest(float[] array)
    {
        int max = 0;
        for (int i = 1; i < array.length; i++)
            if (array[i] > array[max])
                max = i;
        return max;
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("prediction", (String) textViewPrediction.getText());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            textViewPrediction.setText(savedInstanceState.getString("prediction"));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo);
        textViewPrediction = findViewById(R.id.textViewPrediction);
        buttonMakeFoto = findViewById(R.id.buttonMakeFoto);
        PreviewView previewView = findViewById(R.id.previewView2);

        ListenableFuture cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();
                cameraProvider.bindToLifecycle(
                        ((LifecycleOwner) this),
                        cameraSelector,
                        preview);
                preview.setSurfaceProvider(
                        previewView.getSurfaceProvider());
            } catch (InterruptedException | ExecutionException e) {
            }
        }, ContextCompat.getMainExecutor(this));

        buttonMakeFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = previewView.getBitmap();
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
                    MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(getApplicationContext(), "model.tflite");
                    interpreter = new Interpreter(tfliteModel, new Interpreter.Options());
                } catch (Exception e) {
                }
                TensorBuffer output = TensorBuffer.createFixedSize(new int[]{1, 4}, DataType.FLOAT32);
                if (interpreter != null) {
                    interpreter.run(pixelRGBFloat32, output.getBuffer().rewind());
                }
                float predictions[] = output.getFloatArray();
                textViewPrediction.setText(" " + labels[indexOfLargest(predictions)] + " " + String.format("%.2f", predictions[indexOfLargest(predictions)] * 100) + "%");
            }
            });

    }

}
