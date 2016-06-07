package android_serialport_api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android.fpi.MtGpio;
import android.os.Handler;

public class BarcodeReader {
	private static BarcodeReader instance;
	
	public static final int STATE_BARCODE = 1;
	
	private SerialPort mSerialPort = null;
	protected OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;
	private byte[] databuf=new byte[1024];
	private int datasize=0;
    
    private Handler msgHandler=null;
    
    public static BarcodeReader getInstance() {
    	if(null == instance) {
    		instance = new BarcodeReader();
    	}
    	return instance;
    }
    
	private class ReadThread extends Thread {
		@Override
		public void run() {
			super.run();
			while(!isInterrupted()/*true*/) {
				int size;
				try {
					byte[] buffer = new byte[64];
					if (mInputStream == null) return;
					size = mInputStream.read(buffer);
					if (size > 0) {
						onDataReceived(buffer, size);
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}
	
	public void SetMessageHandler(Handler handler){
    	msgHandler=handler;
    }
	
	private void SendMssage(int cmd,int state,int size,byte[] buffer){
		if(msgHandler!=null)
			msgHandler.obtainMessage(cmd,state,size,buffer).sendToTarget();
    }
	
	protected void onDataReceived(final byte[] buffer, final int size) {
		System.arraycopy(buffer, 0, databuf,datasize,size);					
		datasize=datasize+size;
		
		try {
			Thread.currentThread();
			Thread.sleep(1000);
		}catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		byte tp[]=new byte[datasize];
		System.arraycopy(databuf, 0, tp,0,datasize);
		SendMssage(STATE_BARCODE,1,datasize,tp);
		datasize=0;
	}
	
	public void openSerialPort(){
    	try {
			mSerialPort = getSerialPort();
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();
			/* Create a receiving thread */
			mReadThread = new ReadThread();
			mReadThread.start();
		} catch (SecurityException e) {
			//DisplayError(R.string.error_security);
		} catch (IOException e) {
			//DisplayError(R.string.error_unknown);
		} catch (InvalidParameterException e) {
			//DisplayError(R.string.error_configuration);
		}
    }
	
	public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
		if (mSerialPort == null) {
			String path = "/dev/ttyMT1";
			int baudrate = 9600;
			if ( (path.length() == 0) || (baudrate == -1)) {
				throw new InvalidParameterException();
			}
			mSerialPort = new SerialPort(new File(path), baudrate, 0);
		}
		return mSerialPort;
	}
	
	public void closeSerialPort() {
		if (mReadThread != null)
			mReadThread.interrupt();
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
	}
	
	public void BarcodeClose(){    	
		MtGpio.getInstance().BCPowerSwitch(false);
		MtGpio.getInstance().BCReadSwitch(true);
    }
	
	public void BarcodeOpen(){
		MtGpio.getInstance().BCPowerSwitch(true);		
    	MtGpio.getInstance().BCReadSwitch(true);
		try {
			Thread.currentThread();
			Thread.sleep(200);
		}catch (InterruptedException e)
		{
			e.printStackTrace();
		}
    	datasize=0;
    	MtGpio.getInstance().BCReadSwitch(false);    	
    	//DialogFactory.showProgressDialog(RoomCheckActivity.this,"��ȡ������...");
    }
}
