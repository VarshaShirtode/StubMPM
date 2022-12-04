package com.nz.stubmpm;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.nz.stubmpm.R;
import com.pos.sdk.DeviceManager;
import com.pos.sdk.DevicesFactory;
import com.pos.sdk.callback.ResultCallback;
import com.pos.sdk.magcard.IMagCardListener;
import com.pos.sdk.magcard.MagCardDevice;
import com.pos.sdk.magcard.TrackData;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SwipeActivity extends AppCompatActivity {
    TextView btnStart,btnStop;
    TextView txtShow;
    public MagCardDevice mMagCardDevice;
    IMagCardListener iMagCardListener;
    String message="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe);
        btnStart=findViewById(R.id.btnStart);
        btnStop=findViewById(R.id.btnStop);
        txtShow=findViewById(R.id.txtShow);
      /*  DevicesFactory.create(this, new ResultCallback<DeviceManager>() {
            @Override
            public void onFinish(DeviceManager deviceManager) {
                showNormalMessage("ready");
            }

            @Override
            public void onError(int i, String s) {
                showNormalMessage(s + "_" + i);
            }
        });*/
        DevicesFactory.create(this, new ResultCallback<DeviceManager>() {
            @Override
            public void onFinish(DeviceManager deviceManager) {
                mMagCardDevice = deviceManager.getMagneticDevice();
                onSwipeCard();
                showNormalMessage("Created");
            }

            @Override
            public void onError(int i, String s) {
            }
        });


                btnStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onSwipeCard();
                    }
                });

      btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMagCardDevice.stopSwipeCard();
            }
        });

       /* */

      /*  mMagCardDevice.swipeCard(20000, true, new IMagCardListener.Stub(){

            @Override
            public void onSwipeCardTimeout() throws RemoteException {
                showErrorMessage("Swipe card time out");
            }

            @Override
            public void onSwipeCardException(int i) throws RemoteException {
                showErrorMessage("Swipe card error " + i);
            }

            @Override
            public void onSwipeCardSuccess(TrackData trackData) throws RemoteException {
                showNormalMessage("Card number \n" + trackData.getCardno());
                showNormalMessage("Desensitization card number \n" + trackData.getEncryptCardNo());
                showNormalMessage("First track data \n" + trackData.getFirstTrackData());
                showNormalMessage("Second track data \n" + trackData.getSecondTrackData());
                showNormalMessage("Third track data \n" + trackData.getThirdTrackData());
                showNormalMessage("Encrypt the second and third track information \n" + trackData.getEncryptTrackData());
                showNormalMessage("Validity of card \n" + trackData.getExpiryDate());
                showNormalMessage("Service code \n" + trackData.getServiceCode());
            }

            @Override
            public void onSwipeCardFail() throws RemoteException {
                showErrorMessage("Swipe card failed ");
            }

            @Override
            public void onCancelSwipeCard() throws RemoteException {
                showNormalMessage("Swipe card canceled");
            }
        });*/
    }
    private void displayData(String cardno) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                // Stuff that updates the UI
                txtShow.setText("Data "+cardno);
                Log.v("DATARESPONSE",cardno);
            }
        });

        // Toast.makeText(SwipeActivity.this," "+cardno,Toast.LENGTH_SHORT).show();
    }

    private void onSwipeCard() {
      showNormalMessage("Please swipe card");
        mMagCardDevice.swipeCard(20000, true, new IMagCardListener.Stub(){

            @Override
            public void onSwipeCardTimeout() throws RemoteException {
                showErrorMessage("Swipe card time out");
            }

            @Override
            public void onSwipeCardException(int i) throws RemoteException {
                showErrorMessage("Swipe card error " + i);
            }

            @Override
            public void onSwipeCardSuccess(TrackData trackData) throws RemoteException {
                showNormalMessage("Card number " + trackData.getCardno()+"\n Expiry Date "+trackData.getExpiryDate());

               // displayData(""+trackData.getCardno());



            }

            @Override
            public void onSwipeCardFail() throws RemoteException {
                showErrorMessage("Swipe card failed ");
            }

            @Override
            public void onCancelSwipeCard() throws RemoteException {
                showNormalMessage("Swipe card canceled");
            }
        });
    }


    private void showNormalMessage(String please_swipe_card) {
       // txtShow.setText("Data "+please_swipe_card);
        Log.v("DATARESPONSE","Normal "+please_swipe_card);
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                txtShow.setText(please_swipe_card);
            }
        });
    }
    private void showErrorMessage(String please_swipe_card) {

        Log.v("DATARESPONSE",""+please_swipe_card);
    }
}