package hyzk.fingerd;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.sql.BatchUpdateException;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.AsyncFingerprint;
import android_serialport_api.SerialPortManager;

public class MainActivity extends AppCompatActivity {
    private static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;

    private AsyncFingerprint vFingerprint;
    private boolean			 bIsUpImage=true;
    private boolean			 bIsCancel=false;
    private int				 iMatchId=0;
    private boolean			 bfpWork=false;

    private TextView tvFpStatus;
    private ImageView fpImage;

    private Timer fpTimer=null;
    private TimerTask fpTask=null;
    private Handler fpHandler;
    private static boolean fpTimeOut=true;

    private int	iFinger=0;
    private Dialog fpDialog;
    private boolean bcheck=false;
    private int count;
    //finger
    private byte[] model1;
    //private byte[] model2;

    private Button loginBtn;
    private Button checkBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginBtn = (Button)findViewById(R.id.login);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FPDialogInput(1);
            }
        });

        checkBtn= (Button)findViewById(R.id.check);
        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FPDialogCheck(1);
            }
        });
    }

    private void FPDialogInput(int i){
        iFinger=i;
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.txt_fingerplace));
        final LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View vl = inflater.inflate(R.layout.dialog_enrolfinger, null);
        fpImage = (ImageView) vl.findViewById(R.id.imageView1);
        tvFpStatus= (TextView) vl.findViewById(R.id.textview1);
        builder.setView(vl);
        builder.setCancelable(false);
        builder.setNegativeButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SerialPortManager.getInstance().closeSerialPort();
                dialog.dismiss();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                SerialPortManager.getInstance().closeSerialPort();
                dialog.dismiss();
            }
        });

        fpDialog = builder.create();
        fpDialog.setCanceledOnTouchOutside(false);
        fpDialog.show();

        vFingerprint = SerialPortManager.getInstance().getNewAsyncFingerprint();
        FPProcessSave();
    }
    private void FPProcessSave(){
        //??????
        vFingerprint.setOnGetImageExListener(new AsyncFingerprint.OnGetImageExListener() {
            @Override
            public void onGetImageExSuccess() {
                if(bcheck){
                    vFingerprint.PS_GetImageEx();
                }else{
                    if(bIsUpImage){
                        vFingerprint.PS_UpImageEx();
                        tvFpStatus.setText(getString(R.string.txt_fg_read));
                    }else{
                        tvFpStatus.setText(getString(R.string.txt_fg_process));
                        vFingerprint.PS_GenCharEx(count);
                    }
                }
            }

            @Override
            public void onGetImageExFail() {
                if(bcheck){
                    bcheck=false;
                    tvFpStatus.setText(getString(R.string.txt_fg_input_failed));
                    vFingerprint.PS_GetImageEx();
                    count++;
                }else{
                    vFingerprint.PS_GetImageEx();
                }
            }
        });

        vFingerprint.setOnUpImageExListener(new AsyncFingerprint.OnUpImageExListener() {
            @Override
            public void onUpImageExSuccess(byte[] data) {
                Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
                fpImage.setImageBitmap(image);
                vFingerprint.PS_GenCharEx(count);
                tvFpStatus.setText(getString(R.string.txt_fg_rd_success));
            }

            @Override
            public void onUpImageExFail() {
            }
        });

        vFingerprint.setOnGenCharExListener(new AsyncFingerprint.OnGenCharExListener() {
            @Override
            public void onGenCharExSuccess(int bufferId) {
                if (bufferId == 1) {
                    bcheck=true;
                    tvFpStatus.setText(getString(R.string.txt_fingerplace2));
                    vFingerprint.PS_GetImageEx();
                } else if (bufferId == 2) {
                    vFingerprint.PS_RegModel();
                }
            }

            @Override
            public void onGenCharExFail() {
                tvFpStatus.setText(getString(R.string.txt_fg_gen_failed));
            }
        });


        vFingerprint.setOnRegModelListener(new AsyncFingerprint.OnRegModelListener() {

            @Override
            public void onRegModelSuccess() {
                vFingerprint.PS_UpChar();
                tvFpStatus.setText(getString(R.string.txt_fp_bidui_ok));
            }

            @Override
            public void onRegModelFail() {
                tvFpStatus.setText(getString(R.string.txt_fp_bidui_failed));
            }
        });

        vFingerprint.setOnUpCharListener(new AsyncFingerprint.OnUpCharListener() {

            @Override
            public void onUpCharSuccess(byte[] model) {
                model1 = model;
                tvFpStatus.setText(getString(R.string.txt_fg_input_ok));

                SerialPortManager.getInstance().closeSerialPort();
                fpDialog.cancel();
            }

            @Override
            public void onUpCharFail() {
                tvFpStatus.setText(getString(R.string.txt_fg_input_failed));
            }
        });

        vFingerprint.setOnSearchListener(new AsyncFingerprint.OnSearchListener() {
            @Override
            public void onSearchSuccess(int pageId, int matchScore) {
                tvFpStatus.setText("search success");
            }

            @Override
            public void onSearchFail() {
                tvFpStatus.setText("search failed");
            }
        });
        count = 1;
        //model = null;
        tvFpStatus.setText(getString(R.string.txt_fingerplace));
        try {
            Thread.currentThread();
            Thread.sleep(200);
        }catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        vFingerprint.PS_GetImageEx();
    }

    private void FPDialogCheck(int i){
        iFinger=i;
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.txt_fingerplace));
        final LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View vl = inflater.inflate(R.layout.dialog_enrolfinger, null);
        fpImage = (ImageView) vl.findViewById(R.id.imageView1);
        tvFpStatus= (TextView) vl.findViewById(R.id.textview1);
        builder.setView(vl);
        builder.setCancelable(false);
        builder.setNegativeButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SerialPortManager.getInstance().closeSerialPort();
                dialog.dismiss();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                SerialPortManager.getInstance().closeSerialPort();
                dialog.dismiss();
            }
        });

        fpDialog = builder.create();
        fpDialog.setCanceledOnTouchOutside(false);
        fpDialog.show();

        vFingerprint = SerialPortManager.getInstance().getNewAsyncFingerprint();
        FPCheck();
    }

    private void FPCheck(){
        vFingerprint.setOnGetImageExListener(new AsyncFingerprint.OnGetImageExListener() {
            @Override
            public void onGetImageExSuccess() {
                if(bIsUpImage){
                    vFingerprint.PS_UpImageEx();
                    tvFpStatus.setText(getString(R.string.txt_fg_read));

                }else{
                    tvFpStatus.setText(getString(R.string.txt_fg_process));
                    vFingerprint.PS_GenCharEx(1);
                }
            }

            @Override
            public void onGetImageExFail() {
                if(!bIsCancel)
                    vFingerprint.PS_GetImageEx();
            }
        });

        vFingerprint.setOnUpImageExListener(new AsyncFingerprint.OnUpImageExListener() {
            @Override
            public void onUpImageExSuccess(byte[] data) {
                Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
                fpImage.setImageBitmap(image);
                vFingerprint.PS_GenCharEx(1);
                tvFpStatus.setText(getString(R.string.txt_fg_rd_success));
                fpTimeOut=false;
            }

            @Override
            public void onUpImageExFail() {
                bfpWork=false;
            }
        });

        vFingerprint.setOnGenCharExListener(new AsyncFingerprint.OnGenCharExListener() {
            @Override
            public void onGenCharExSuccess(int bufferId) {
                //vFingerprint.PS_Search(1, 1, 256);
                tvFpStatus.setText(getString(R.string.txt_fg_identify));
                vFingerprint.PS_DownChar(model1);

            }

            @Override
            public void onGenCharExFail() {
                tvFpStatus.setText(getString(R.string.txt_fg_gen_failed));
                bfpWork=false;
            }
        });

        vFingerprint.setOnDownCharListener(new AsyncFingerprint.OnDownCharListener(){
            @Override
            public void onDownCharSuccess() {
                vFingerprint.PS_Match();
            }

            @Override
            public void onDownCharFail() {
                tvFpStatus.setText(getString(R.string.txt_fg_failed));
                SerialPortManager.getInstance().closeSerialPort();
                bfpWork=false;
            }
        });

        vFingerprint.setOnMatchListener(new AsyncFingerprint.OnMatchListener(){
            @Override
            public void onMatchSuccess() {
                tvFpStatus.setText(getString(R.string.txt_fg_ok));
                SerialPortManager.getInstance().closeSerialPort();
                bfpWork=false;
            }

            @Override
            public void onMatchFail() {
                tvFpStatus.setText(getString(R.string.txt_fg_failed));
                SerialPortManager.getInstance().closeSerialPort();
                bfpWork=false;
            }
        });

        vFingerprint.setOnSearchListener(new AsyncFingerprint.OnSearchListener() {
            @Override
            public void onSearchSuccess(int pageId, int matchScore) {
                tvFpStatus.setText(getString(R.string.txt_fg_ok));
            }

            @Override
            public void onSearchFail() {
                tvFpStatus.setText(getString(R.string.txt_fg_failed));
            }
        });

        try {
            Thread.currentThread();
            Thread.sleep(200);
        }catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        vFingerprint.PS_GetImageEx();
    }

}
