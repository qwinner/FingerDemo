package android_serialport_api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android.fpi.MtGpio;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

public class SerialPortManager {
	
	//�����豸·��
	private static final String PATH = "/dev/ttyMT3";
	//���ڲ�����
	//private static final int BAUDRATE = 115200;
	//private static final int BAUDRATE = 230400;
	private static final int BAUDRATE = 460800;
		
	private static SerialPortManager mSerialPortManager = new SerialPortManager();
	private SerialPort mSerialPort = null;
	private boolean isOpen;
	private boolean firstOpen = false;

	private OutputStream mOutputStream;
	private InputStream mInputStream;
	private byte[] mBuffer = new byte[100 * 1024];
	private int mCurrentSize = 0;
	private Looper looper;
	private HandlerThread ht;
	private ReadThread mReadThread;

	public AsyncFingerprint getNewAsyncFingerprint() {
		if (!isOpen) {
			try {
				openSerialPort();
				isOpen = true;
			} catch (InvalidParameterException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new AsyncFingerprint(looper);
	}

	public SerialPortManager() {
	}

	public static SerialPortManager getInstance() {
		return mSerialPortManager;
	}

	public boolean isOpen() {
		return isOpen;
	}
	
	public boolean isFirstOpen() {
		return firstOpen;
	}

	public void setFirstOpen(boolean firstOpen) {
		this.firstOpen = firstOpen;
	}

	private void createWorkThread() {
		ht = new HandlerThread("workerThread");
		ht.start();
		looper = ht.getLooper();
	}

	public void openSerialPort() throws SecurityException, IOException,
			InvalidParameterException {
		if (mSerialPort == null) {
			MtGpio.getInstance().FPPowerSwitch(true);
			/* Open the serial port */
			mSerialPort = new SerialPort(new File(PATH), BAUDRATE, 0);
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();
			mReadThread = new ReadThread();
			mReadThread.start();
			isOpen = true;
			createWorkThread();
			firstOpen = true;
		}
	}

	public void closeSerialPort() {
		if (ht != null) {
			ht.quit();
		}
		ht = null;
		if (mReadThread != null)
			mReadThread.interrupt();
		mReadThread = null;
		MtGpio.getInstance().FPPowerSwitch(false);
		if (mSerialPort != null) {
			try {
				mOutputStream.close();
				mInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mSerialPort.close();
			mSerialPort = null;
		}
		isOpen = false;
		mCurrentSize = 0;
	}

	protected synchronized int read(byte buffer[], int waittime) {
		int time = 1000; //4000
		int sleepTime = 50;
		int length = time / sleepTime;
		boolean shutDown = false;
		int[] readDataLength = new int[3];
		for (int i = 0; i < length; i++) {
			if (mCurrentSize == 0) {
				SystemClock.sleep(sleepTime);
				continue;
			} else {
				break;
			}
		}

		if (mCurrentSize > 0) {
			while (!shutDown) {
				SystemClock.sleep(sleepTime);
				readDataLength[0] = readDataLength[1];
				readDataLength[1] = readDataLength[2];
				readDataLength[2] = mCurrentSize;
				Log.i("whw", "read2    mCurrentSize=" + mCurrentSize);
				if (readDataLength[0] == readDataLength[1]
						&& readDataLength[1] == readDataLength[2]) {
					shutDown = true;
				}
			}
			if (mCurrentSize <= buffer.length) {
				System.arraycopy(mBuffer, 0, buffer, 0, mCurrentSize);
			}
		}
		return mCurrentSize;
	}

	protected synchronized void write(byte[] data) throws IOException {
		mCurrentSize = 0;
		mOutputStream.write(data);
	}

	private class ReadThread extends Thread {

		@Override
		public void run() {
			while (!isInterrupted()) {
				int length = 0;
				try {
					byte[] buffer = new byte[100];
					if (mInputStream == null)
						return;
					length = mInputStream.read(buffer);
					if (length > 0) {
						System.arraycopy(buffer, 0, mBuffer, mCurrentSize,
								length);
						mCurrentSize += length;
					}
					Log.i("whw", "mCurrentSize=" + mCurrentSize + "  length="
							+ length);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

}
