package android_serialport_api;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;



import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import hyzk.fingerd.DataUtils;

public class AsyncFingerprint extends Handler {

	private static final int PS_GetImage = 0x01;
	private static final int PS_GenChar = 0x02;
	private static final int PS_Match = 0x03;
	private static final int PS_Search = 0x04;
	private static final int PS_RegModel = 0x05;
	private static final int PS_StoreChar = 0x06;
	private static final int PS_LoadChar = 0x07;
	private static final int PS_UpChar = 0x08;

	private static final int PS_DownChar = 0x09;
	private static final int PS_UpImage = 0x0a;
	private static final int PS_DownImage = 0x0b;
	private static final int PS_DeleteChar = 0x0c;
	private static final int PS_Empty = 0x0d;
	private static final int PS_Enroll = 0x10;
	private static final int PS_Identify = 0x11;
	private Handler mWorkerThreadHandler;
	
	private static final int  PS_GetImageEX = 0x30;
	private static final int  PS_UpImageEX = 0x31;
	private static final int  PS_GenCharEX = 0x32;
	

	private static final int UP_IMAGE_RESPONSE_SIZE = 40044;
	
	private static final int UP_IMAGEEX_RESPONSE_SIZE = 16521;
	

	private static final int UP_CHAR_RESPONSE_SIZE = 568;


	private byte[] data = new byte[1024 * 50];

	private byte[] buffer = new byte[1024 * 50];

	protected AsyncFingerprint(Looper looper) {
		createHandler(looper);
	}

	private Handler createHandler(Looper looper) {
		return mWorkerThreadHandler = new WorkerHandler(looper);
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
		case PS_GetImage:
			if (onGetImageListener == null) {
				return;
			}
			if (msg.arg1 == 0) {
				onGetImageListener.onGetImageSuccess();
			} else {
				onGetImageListener.onGetImageFail();
			}
			break;
		case PS_UpImage:
			if (onUpImageListener == null) {
				return;
			}
			if (msg.obj != null) {
				onUpImageListener.onUpImageSuccess((byte[]) msg.obj);
			} else {
				onUpImageListener.onUpImageFail();
			}
			break;
		case PS_GenChar:
			if (onGenCharListener == null) {
				return;
			} else {
				if (msg.arg1 == 0) {
					onGenCharListener.onGenCharSuccess(msg.arg2);
				} else {
					onGenCharListener.onGenCharFail();
				}
			}
			break;
		case PS_RegModel:
			if (onRegModelListener == null) {
				return;
			} else {
				if (msg.arg1 == 0) {
					Log.i("whw", "onRegModelListener");
					onRegModelListener.onRegModelSuccess();
				} else {
					onRegModelListener.onRegModelFail();
				}
			}
			break;
		case PS_UpChar:
			if (onUpCharListener == null) {
				return;
			} else {
				if (msg.obj != null) {
					onUpCharListener.onUpCharSuccess((byte[]) msg.obj);
				} else {
					onUpCharListener.onUpCharFail();
				}
			}
			break;
		case PS_DownChar:
			if (onDownCharListener == null) {
				return;
			} else {
				if (msg.arg1 == 0) {
					onDownCharListener.onDownCharSuccess();
				} else {
					onDownCharListener.onDownCharFail();
				}
			}
			break;
		case PS_Match:
			if (onMatchListener == null) {
				return;
			} else {
				if ((Boolean) msg.obj) {
					onMatchListener.onMatchSuccess();
				} else {
					onMatchListener.onMatchFail();
				}
			}
			break;
		case PS_StoreChar:
			if (onStoreCharListener == null) {
				return;
			} else {
				if (msg.arg1 == 0) {
					onStoreCharListener.onStoreCharSuccess();
				} else {
					onStoreCharListener.onStoreCharFail();
				}
			}
			break;
		case PS_LoadChar:
			if (onLoadCharListener == null) {
				return;
			} else {
				if (msg.arg1 == 0) {
					onLoadCharListener.onLoadCharSuccess();
				} else {
					onLoadCharListener.onLoadCharFail();
				}
			}
			break;
		case PS_Search:
			if (onSearchListener == null) {
				return;
			} else {
				byte[] result = (byte[]) msg.obj;
				if (result != null) {
					if (result[9] == 0x00) {
						short pageId = getShort(result[10], result[11]);
						short matchScore = getShort(result[12], result[13]);
						onSearchListener.onSearchSuccess(pageId, matchScore);
						return;
					}
				}
				onSearchListener.onSearchFail();
			}
			break;
		case PS_DeleteChar:
			if (onDeleteCharListener == null) {
				return;
			} else {
				if (msg.arg1 == 0) {
					onDeleteCharListener.onDeleteCharSuccess();
				} else {
					onDeleteCharListener.onDeleteCharFail();
				}
			}
			break;
		case PS_Empty:
			if (onEmptyListener == null) {
				return;
			} else {
				if (msg.arg1 == 0) {
					onEmptyListener.onEmptySuccess();
				} else {
					onEmptyListener.onEmptyFail();
				}
			}
			break;
		case PS_Enroll:
			if (onEnrollListener == null) {
				return;
			} else {
				byte[] result = (byte[]) msg.obj;
				if (result != null) {
					if (result[9] == 0x00) {
						short pageId = getShort(result[10], result[11]);
						onEnrollListener.onEnrollSuccess(pageId);
						return;
					}
				}
				onEnrollListener.onEnrollFail();
			}
			break;
		case PS_Identify:
			if (onIdentifyListener == null) {
				return;
			} else {
				byte[] result = (byte[]) msg.obj;
				if (result != null) {
					if (result[9] == 0x00) {
						short pageId = getShort(result[10], result[11]);
						short matchScore = getShort(result[12], result[13]);
						onIdentifyListener
								.onIdentifySuccess(pageId, matchScore);
						return;
					}
				}
				onIdentifyListener.onIdentifyFail();
			}
			break;
		case PS_GetImageEX:
			if (onGetImageExListener == null) {
				return;
			}
			if (msg.arg1 == 0) {
				onGetImageExListener.onGetImageExSuccess();
			} else {
				onGetImageExListener.onGetImageExFail();
			}
			break;
		case PS_UpImageEX:
			if (onUpImageExListener == null) {
				return;
			}
			if (msg.obj != null) {
				onUpImageExListener.onUpImageExSuccess((byte[]) msg.obj);
			} else {
				onUpImageExListener.onUpImageExFail();
			}
			break;
		case PS_GenCharEX:
			if (onGenCharExListener == null) {
				return;
			} else {
				if (msg.arg1 == 0) {
					onGenCharExListener.onGenCharExSuccess(msg.arg2);
				} else {
					onGenCharExListener.onGenCharExFail();
				}
			}
			break;
		default:
			break;
		}
	}

	protected class WorkerHandler extends Handler {
		public WorkerHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PS_GetImage:
				int valueGetImage = PSGetImage();
				AsyncFingerprint.this.obtainMessage(PS_GetImage, valueGetImage,
						-1).sendToTarget();
				break;
			case PS_UpImage:
				byte[] imageData = PSUpImage();
				AsyncFingerprint.this.obtainMessage(PS_UpImage, imageData)
						.sendToTarget();
				break;
			case PS_GenChar:
				int valueGenChar = PSGenChar(msg.arg1);
				AsyncFingerprint.this.obtainMessage(PS_GenChar, valueGenChar,
						msg.arg1).sendToTarget();
				break;
			case PS_RegModel:
				int valueRegModel = PSRegModel();
				AsyncFingerprint.this.obtainMessage(PS_RegModel, valueRegModel,
						-1).sendToTarget();
				break;
			case PS_UpChar:
				byte[] charData = PSUpChar();
				AsyncFingerprint.this.obtainMessage(PS_UpChar, charData)
						.sendToTarget();
				break;
			case PS_DownChar:
				int valueDownChar = PSDownChar((byte[]) msg.obj);
				AsyncFingerprint.this.obtainMessage(PS_DownChar, valueDownChar,
						-1).sendToTarget();
				break;
			case PS_Match:
				boolean valueMatch = PSMatch();
				AsyncFingerprint.this.obtainMessage(PS_Match,
						Boolean.valueOf(valueMatch)).sendToTarget();
				break;
			case PS_StoreChar:
				int valueStoreChar = PSStoreChar(msg.arg1, msg.arg2);
				AsyncFingerprint.this.obtainMessage(PS_StoreChar,
						valueStoreChar, -1).sendToTarget();
				break;
			case PS_LoadChar:
				int valueLoadChar = PSLoadChar(msg.arg1, msg.arg2);
				AsyncFingerprint.this.obtainMessage(PS_LoadChar, valueLoadChar,
						-1).sendToTarget();
				break;
			case PS_Search:
				byte[] result = PSSearch(msg.arg1, msg.arg2, (Integer) msg.obj);
				AsyncFingerprint.this.obtainMessage(PS_Search, result)
						.sendToTarget();
				break;
			case PS_DeleteChar:
				int valueDeleteChar = PSDeleteChar((short) msg.arg1,
						(short) msg.arg2);
				AsyncFingerprint.this.obtainMessage(PS_DeleteChar,
						valueDeleteChar, -1).sendToTarget();
				break;
			case PS_Empty:
				int valueEmpty = PSEmpty();
				AsyncFingerprint.this.obtainMessage(PS_Empty, valueEmpty, -1)
						.sendToTarget();
				break;
			case PS_Enroll:
				byte[] valueEnroll = PSEnroll();
				AsyncFingerprint.this.obtainMessage(PS_Enroll, valueEnroll)
						.sendToTarget();
				break;
			case PS_Identify:
				byte[] valueIdentify = PSIdentify();
				AsyncFingerprint.this.obtainMessage(PS_Identify, valueIdentify)
						.sendToTarget();
				break;
			case PS_GetImageEX:
				int valueGetImageEx = PSGetImageEx();
				AsyncFingerprint.this.obtainMessage(PS_GetImageEX, valueGetImageEx,
						-1).sendToTarget();
				break;
			case PS_UpImageEX:
				byte[] imageDataEx = PSUpImageEx();
				AsyncFingerprint.this.obtainMessage(PS_UpImageEX, imageDataEx)
						.sendToTarget();
				break;
			case PS_GenCharEX:
				int valueGenCharEx = PSGenCharEx(msg.arg1);
				AsyncFingerprint.this.obtainMessage(PS_GenCharEX, valueGenCharEx,
						msg.arg1).sendToTarget();
				break;
			default:
				break;
			}
		}
	}

	private OnGetImageListener onGetImageListener;

	private OnUpImageListener onUpImageListener;

	private OnGenCharListener onGenCharListener;

	private OnRegModelListener onRegModelListener;

	private OnUpCharListener onUpCharListener;

	private OnDownCharListener onDownCharListener;

	private OnMatchListener onMatchListener;

	private OnStoreCharListener onStoreCharListener;

	private OnLoadCharListener onLoadCharListener;

	private OnSearchListener onSearchListener;

	private OnDeleteCharListener onDeleteCharListener;

	private OnEmptyListener onEmptyListener;

	private OnEnrollListener onEnrollListener;

	private OnIdentifyListener onIdentifyListener;
	
	private OnGetImageExListener onGetImageExListener;
	private OnUpImageExListener onUpImageExListener;
	private OnGenCharExListener onGenCharExListener;
	
	public void setOnGetImageListener(OnGetImageListener onGetImageListener) {
		this.onGetImageListener = onGetImageListener;
	}

	public void setOnUpImageListener(OnUpImageListener onUpImageListener) {
		this.onUpImageListener = onUpImageListener;
	}

	public void setOnGenCharListener(OnGenCharListener onGenCharListener) {
		this.onGenCharListener = onGenCharListener;
	}

	public void setOnRegModelListener(OnRegModelListener onRegModelListener) {
		this.onRegModelListener = onRegModelListener;
	}

	public void setOnUpCharListener(OnUpCharListener onUpCharListener) {
		this.onUpCharListener = onUpCharListener;
	}

	public void setOnDownCharListener(OnDownCharListener onDownCharListener) {
		this.onDownCharListener = onDownCharListener;
	}

	public void setOnMatchListener(OnMatchListener onMatchListener) {
		this.onMatchListener = onMatchListener;
	}

	public void setOnStoreCharListener(OnStoreCharListener onStoreCharListener) {
		this.onStoreCharListener = onStoreCharListener;
	}

	public void setOnLoadCharListener(OnLoadCharListener onLoadCharListener) {
		this.onLoadCharListener = onLoadCharListener;
	}

	public void setOnSearchListener(OnSearchListener onSearchListener) {
		this.onSearchListener = onSearchListener;
	}

	public void setOnDeleteCharListener(
			OnDeleteCharListener onDeleteCharListener) {
		this.onDeleteCharListener = onDeleteCharListener;
	}

	public void setOnEmptyListener(OnEmptyListener onEmptyListener) {
		this.onEmptyListener = onEmptyListener;
	}

	public void setOnEnrollListener(OnEnrollListener onEnrollListener) {
		this.onEnrollListener = onEnrollListener;
	}

	public void setOnIdentifyListener(OnIdentifyListener onIdentifyListener) {
		this.onIdentifyListener = onIdentifyListener;
	}

	public void setOnGetImageExListener(OnGetImageExListener onGetImageExListener) {
		this.onGetImageExListener = onGetImageExListener;
	}

	public void setOnUpImageExListener(OnUpImageExListener onUpImageExListener) {
		this.onUpImageExListener = onUpImageExListener;
	}

	public void setOnGenCharExListener(OnGenCharExListener onGenCharExListener) {
		this.onGenCharExListener = onGenCharExListener;
	}

	
	public interface OnGetImageListener {
		void onGetImageSuccess();

		void onGetImageFail();
	}

	public interface OnUpImageListener {
		void onUpImageSuccess(byte[] data);

		void onUpImageFail();
	}

	public interface OnGenCharListener {
		void onGenCharSuccess(int bufferId);

		void onGenCharFail();
	}

	public interface OnRegModelListener {
		void onRegModelSuccess();

		void onRegModelFail();
	}

	public interface OnUpCharListener {
		void onUpCharSuccess(byte[] model);

		void onUpCharFail();
	}

	public interface OnDownCharListener {
		void onDownCharSuccess();

		void onDownCharFail();
	}

	public interface OnMatchListener {
		void onMatchSuccess();

		void onMatchFail();
	}

	public interface OnStoreCharListener {
		void onStoreCharSuccess();

		void onStoreCharFail();
	}

	public interface OnLoadCharListener {
		void onLoadCharSuccess();

		void onLoadCharFail();
	}

	public interface OnSearchListener {
		void onSearchSuccess(int pageId, int matchScore);

		void onSearchFail();
	}

	public interface OnDeleteCharListener {
		void onDeleteCharSuccess();

		void onDeleteCharFail();
	}

	public interface OnEmptyListener {
		void onEmptySuccess();

		void onEmptyFail();
	}

	public interface OnEnrollListener {
		void onEnrollSuccess(int pageId);

		void onEnrollFail();
	}

	public interface OnIdentifyListener {
		void onIdentifySuccess(int pageId, int matchScore);

		void onIdentifyFail();
	}

	public interface OnGetImageExListener {
		void onGetImageExSuccess();

		void onGetImageExFail();
	}

	public interface OnUpImageExListener {
		void onUpImageExSuccess(byte[] data);

		void onUpImageExFail();
	}

	public interface OnGenCharExListener {
		void onGenCharExSuccess(int bufferId);

		void onGenCharExFail();
	}
	
	public void PS_GetImage() {
		mWorkerThreadHandler.sendEmptyMessage(PS_GetImage);
	}

	public void PS_UpImage() {
		mWorkerThreadHandler.sendEmptyMessage(PS_UpImage);
	}

	public void PS_GenChar(int bufferId) {
		mWorkerThreadHandler.obtainMessage(PS_GenChar, bufferId, -1)
				.sendToTarget();
	}

	public void PS_RegModel() {
		mWorkerThreadHandler.sendEmptyMessage(PS_RegModel);
	}

	public void PS_UpChar() {
		mWorkerThreadHandler.sendEmptyMessage(PS_UpChar);
	}

	public void PS_DownChar(byte[] model) {
		mWorkerThreadHandler.obtainMessage(PS_DownChar, model).sendToTarget();
	}

	public void PS_Match() {
		mWorkerThreadHandler.sendEmptyMessage(PS_Match);
	}

	public void PS_StoreChar(int bufferId, int pageId) {
		mWorkerThreadHandler.obtainMessage(PS_StoreChar, bufferId, pageId)
				.sendToTarget();
	}

	public void PS_LoadChar(int bufferId, int pageId) {
		mWorkerThreadHandler.obtainMessage(PS_LoadChar, bufferId, pageId)
				.sendToTarget();
	}

	public void PS_Search(int bufferId, int startPageId, int pageNum) {
		mWorkerThreadHandler.obtainMessage(PS_Search, bufferId, startPageId,
				pageNum).sendToTarget();
	}

	public void PS_DeleteChar(int pageIDStart, int delNum) {
		mWorkerThreadHandler.obtainMessage(PS_DeleteChar, pageIDStart, delNum)
				.sendToTarget();
	}

	public void PS_Empty() {
		mWorkerThreadHandler.sendEmptyMessage(PS_Empty);
	}

	public void PS_Enroll() {
		mWorkerThreadHandler.sendEmptyMessage(PS_Enroll);
	}

	public void PS_Identify() {
		mWorkerThreadHandler.sendEmptyMessage(PS_Identify);
	}

	public void PS_GetImageEx() {
		mWorkerThreadHandler.sendEmptyMessage(PS_GetImageEX);
	}

	public void PS_UpImageEx() {
		mWorkerThreadHandler.sendEmptyMessage(PS_UpImageEX);
	}

	public void PS_GenCharEx(int bufferId) {
		mWorkerThreadHandler.obtainMessage(PS_GenCharEX, bufferId, -1)
				.sendToTarget();
	}
	

	private synchronized int PSGetImage() {
		byte[] command = { (byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, 0x01, 0x00, 0x03, 0x01, 0x00, 0x05 };
		sendCommand(command);
		int length = SerialPortManager.getInstance().read(buffer, 50);
		printlog("PSGetImage", length);
		if (length == 12) {
			return buffer[9];
		}
		return -1;
	}


	private synchronized byte PSGenChar(int bufferId) {
		byte[] command = { (byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, (byte) 01, (byte) 0x00, (byte) 0x04,
				(byte) 0x02, (byte) bufferId, (byte) 0x00,
				(byte) (0x7 + bufferId) };
		sendCommand(command);
		int length = SerialPortManager.getInstance().read(buffer, 500);
		printlog("PSGenChar", length);
		if (length == 12) {
			return buffer[9];
		}
		return -1;
	}


	private synchronized byte PSRegModel() {
		byte[] command = { (byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, 0x01, 0x00, 0x03, 0x05, 0x00, 0x09 };
		sendCommand(command);
		int length = SerialPortManager.getInstance().read(buffer, 100);
		printlog("PSRegModel", length);
		if (length == 12) {
			return buffer[9];
		}
		return -1;
	}


	private synchronized byte PSStoreChar(int bufferId, int pageId) {
		byte[] pageIDArray = short2byte((short) pageId);
		// Log.i("whw", "pageid hex=" + DataUtils.toHexString(pageIDArray));
		int checkSum = 0x01 + 0x00 + 0x06 + 0x06 + bufferId
				+ (pageIDArray[0] & 0xff) + (pageIDArray[1] & 0xff);
		byte[] checkSumArray = short2byte((short) checkSum);
		// Log.i("whw",
		// "checkSumArray hex=" + DataUtils.toHexString(checkSumArray)
		// + "    checkSum=" + checkSum);
		byte[] command = { (byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, (byte) 0x01, (byte) 0x00,
				(byte) 0x06, (byte) 0x06, (byte) bufferId,
				(byte) pageIDArray[0], (byte) pageIDArray[1],
				(byte) checkSumArray[0], (byte) checkSumArray[1] };
		sendCommand(command);
		int length = SerialPortManager.getInstance().read(buffer, 100);
		printlog("PSStoreChar", length);
		if (length == 12) {
			return buffer[9];
		}
		return -1;
	}


	private synchronized byte PSLoadChar(int bufferId, int pageId) {
		byte[] pageIDArray = short2byte((short) pageId);
		int checkSum = 0x01 + 0x00 + 0x06 + 0x07 + bufferId
				+ (pageIDArray[0] & 0xff) + (pageIDArray[1] & 0xff);
		byte[] checkSumArray = short2byte((short) checkSum);
		byte[] command = { (byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, (byte) 0x01, (byte) 0x00,
				(byte) 0x06, (byte) 0x07, (byte) bufferId,
				(byte) pageIDArray[0], (byte) pageIDArray[1],
				(byte) checkSumArray[0], (byte) checkSumArray[1] };
		sendCommand(command);
		int length = SerialPortManager.getInstance().read(buffer, 100);
		printlog("PSLoadChar", length);
		if (length == 12) {
			return buffer[9];
		}
		return -1;
	}

	private synchronized byte[] PSSearch(int bufferId, int startPageId,
			int pageNum) {
		byte[] startPageIDArray = short2byte((short) startPageId);
		byte[] pageNumArray = short2byte((short) pageNum);
		int checkSum = 0x01 + 0x00 + 0x08 + 0x04 + bufferId
				+ (startPageIDArray[0] & 0xff) + (startPageIDArray[1] & 0xff)
				+ (pageNumArray[0] & 0xff) + (pageNumArray[1] & 0xff);
		byte[] checkSumArray = short2byte((short) checkSum);
		byte[] command = { (byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, (byte) 0x01, (byte) 0x00,
				(byte) 0x08, (byte) 0x04, (byte) bufferId,
				(byte) startPageIDArray[0], (byte) startPageIDArray[1],
				(byte) pageNumArray[0], (byte) pageNumArray[1],
				(byte) checkSumArray[0], (byte) checkSumArray[1] };
		sendCommand(command);
		int length = SerialPortManager.getInstance().read(buffer, 100);
		printlog("PSSearch", length);
		if (length == 16) {
			byte[] result = new byte[16];
			System.arraycopy(buffer, 0, result, 0, length);
			return result;
		}
		return null;
	}


	private synchronized boolean PSMatch() {
		byte[] command = { (byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, (byte) 0x01, (byte) 0x00,
				(byte) 0x03, (byte) 0x03, (byte) 0x00, (byte) 0x07 };
		sendCommand(command);
		int length = SerialPortManager.getInstance().read(buffer, 100);
		printlog("PSMatch", length);
		if (length == 14) {
			if (buffer[9] == 0x00) {
				return score(buffer[10], buffer[11]);
			}
		}
		return false;
	}

	private synchronized byte[] PSEnroll() {
		byte[] command = { (byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, (byte) 0x01, (byte) 0x00,
				(byte) 0x03, (byte) 0x10, (byte) 0x00, (byte) 0x14 };
		sendCommand(command);
		int length = SerialPortManager.getInstance().read(buffer, 600);
		printlog("PSEnroll", length);
		if (length == 14) {
			byte[] result = new byte[length];
			System.arraycopy(buffer, 0, result, 0, length);
			return result;
		}
		return null;
	}


	private synchronized byte[] PSIdentify() {
		byte[] command = { (byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, (byte) 0x01, (byte) 0x00,
				(byte) 0x03, (byte) 0x11, (byte) 0x00, (byte) 0x15 };
		sendCommand(command);
		int length = SerialPortManager.getInstance().read(buffer, 600);
		printlog("PSIdentify", length);
		if (length == 16) {
			byte[] result = new byte[length];
			System.arraycopy(buffer, 0, result, 0, length);
			return result;
		}
		return null;
	}


	private synchronized byte PSDeleteChar(short pageIDStart, short delNum) {
		byte[] pageIDArray = short2byte(pageIDStart);
		byte[] delNumArray = short2byte(delNum);
		int checkSum = 0x01 + 0x07 + 0x0c + (pageIDArray[0] & 0xff)
				+ (pageIDArray[1] & 0xff) + (delNumArray[0] & 0xff)
				+ (delNumArray[1] & 0xff);
		byte[] checkSumArray = short2byte((short) checkSum);
		byte[] command = { (byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, (byte) 0x01, (byte) 0x00,
				(byte) 0x07, (byte) 0x0c, pageIDArray[0], pageIDArray[1],
				delNumArray[0], delNumArray[1], checkSumArray[0],
				checkSumArray[1] };
		sendCommand(command);
		int length = SerialPortManager.getInstance().read(buffer, 100);
		printlog("PSDeleteChar", length);
		if (length == 12) {
			return buffer[9];
		}
		return -1;
	}

	private synchronized byte PSEmpty() {
		byte[] command = { (byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, (byte) 0x01, (byte) 0x00,
				(byte) 0x03, (byte) 0x0d, (byte) 0x00, (byte) 0x11 };
		sendCommand(command);
		int length = SerialPortManager.getInstance().read(buffer, 100);
		printlog("PSEmpty", length);
		if (length == 12) {
			return buffer[9];
		}
		return -1;
	}

	private synchronized byte[] PSUpChar() {
		byte[] command = { (byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, (byte) 0x01, (byte) 0x00,
				(byte) 0x04, (byte) 0x08, (byte) 0x01, (byte) 0x00, (byte) 0x0e };
		sendCommand(command);
		int length = SerialPortManager.getInstance().read(buffer, 100);
		printlog("PSUpChar", 12);
		if (length >= UP_CHAR_RESPONSE_SIZE) {
			index = 12;
			packetNum = 0;
			byte[] packets = new byte[UP_CHAR_RESPONSE_SIZE];
			System.arraycopy(buffer, 0, packets, 0, UP_CHAR_RESPONSE_SIZE);
			return parsePacketData(packets);
		}
		return null;

	}


	private synchronized byte PSDownChar(byte[] model) {
		byte[] command = { (byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, (byte) 0x01, (byte) 0x00,
				(byte) 0x04, (byte) 0x09, (byte) 0x02, (byte) 0x00, (byte) 0x10 };
		sendCommand(command);
		int length = SerialPortManager.getInstance().read(buffer, 100);
		printlog("PSDownChar", length);
		if (length == 12 && buffer[9] == 0x00) {
			sendData(model);
			return 0x00;
		}
		return -1;
	}


	private synchronized byte[] PSUpImage() {
		byte[] command = { (byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, (byte) 0x01, (byte) 0x00,
				(byte) 0x03, (byte) 0x0a, (byte) 0x00, (byte) 0x0e };
		sendCommand(command);
		int length = SerialPortManager.getInstance().read(buffer, 100);
		Log.i("whw", "PSUpImage length=" + length);
		if (length >= UP_IMAGE_RESPONSE_SIZE) {
			byte[] packets = new byte[length];
			System.arraycopy(buffer, 0, packets, 0, length);
			index = 12;
			packetNum = 0;
			byte[] data = parsePacketData(packets);
			return getFingerprintImage(data);
		}
		return null;

	}
	

	private synchronized int PSGetImageEx() {
		byte[] command = { (byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, 0x01, 0x00, 0x03, 0x30, 0x00, 0x34 };
		sendCommand(command);
		int length = SerialPortManager.getInstance().read(buffer, 50);
		printlog("PSGetImageEx", length);
		if (length == 12) {
			return buffer[9];
		}
		return -1;
	}


	private synchronized byte PSGenCharEx(int bufferId) {
		byte[] command = { (byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, (byte) 01, (byte) 0x00, (byte) 0x04,
				(byte) 0x32, (byte) bufferId, (byte) 0x00,
				(byte) (0x37 + bufferId) };
		sendCommand(command);
		int length = SerialPortManager.getInstance().read(buffer, 500);
		printlog("PSGenCharEx", length);
		if (length == 12) {
			return buffer[9];
		}
		return -1;
	}


	private synchronized byte[] PSUpImageEx() {
		byte[] command = { (byte) 0xef, (byte) 0x01, (byte) 0xff, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, (byte) 0x01, (byte) 0x00,
				(byte) 0x03, (byte) 0x31, (byte) 0x00, (byte) 0x35 };
		sendCommand(command);
		int length = SerialPortManager.getInstance().read(buffer, 100);
		Log.i("whw", "PSUpImageEx length=" + length);
		if (length >= UP_IMAGEEX_RESPONSE_SIZE) {
			byte[] packets = new byte[length];
			System.arraycopy(buffer, 0, packets, 0, length);
			index = 12;
			packetNum = 0;
			byte[] data = parsePacketData(packets);
			return getFingerprintImageEx(data);
		}
		return null;

	}
	

	private void sendData(byte[] data) {

		byte[] dataPrefix = { (byte) 0xef, (byte) 0x01, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x02,
				(byte) 0x00, (byte) 0x82 };

		byte[] endPrefix = { (byte) 0xef, (byte) 0x01, (byte) 0xff,
				(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x08,
				(byte) 0x00, (byte) 0x82 };
		byte[] command = new byte[dataPrefix.length + 128 + 2];
		for (int i = 0; i < 4; i++) {
			if (i == 3) {
				System.arraycopy(endPrefix, 0, command, 0, endPrefix.length);
			} else {
				System.arraycopy(dataPrefix, 0, command, 0, dataPrefix.length);
			}
			System.arraycopy(data, i * 128, command, dataPrefix.length, 128);
			short sum = 0;
			for (int j = 6; j < command.length - 2; j++) {
				sum += (command[j] & 0xff);
			}
			byte[] size = short2byte(sum);
			command[command.length - 2] = size[0];
			command[command.length - 1] = size[1];
			sendCommand(command);
			SystemClock.sleep(20);
		}
	}

	private int index;
	private int packetNum;

	private byte[] parsePacketData(byte[] packet) {
		int dstPos = 0;
		int packageLength = 0;
		int size = 0;
		do {
			packageLength = getShort(packet[index + 7], packet[index + 8]);
			System.arraycopy(packet, index + 9, data, dstPos, packageLength - 2);
			dstPos += packageLength - 2;
			packetNum++;
			size += packageLength - 2;
		} while (moveToNext(index + 6, packageLength, packet));
		if (size != 0) {
			byte[] dataPackage = new byte[size];
			Log.i("whw", "**************packetNum=" + packetNum);
			System.arraycopy(data, 0, dataPackage, 0, size);
			return dataPackage;
		}
		return null;
	}

	private boolean moveToNext(int position, int packageLength, byte[] packet) {
		if (packet[position] == 0x02) {
			index += packageLength + 9;
			return true;
		}
		return false;
	}

	public byte[] getFingerprintImage(byte[] data) {
		if (data == null) {
			return null;
		}
		byte[] imageData = new byte[data.length * 2];
		// Log.i("whw", "*****************data.length="+data.length);
		for (int i = 0; i < data.length; i++) {
			imageData[i * 2] = (byte) (data[i] & 0xf0);
			imageData[i * 2 + 1] = (byte) (data[i] << 4 & 0xf0);
		}

		byte[] bmpData = toBmpByte(256, packetNum, imageData);
		return bmpData;
	}
	
	public byte[] getFingerprintImageEx(byte[] data) {
		if (data == null) {
			return null;
		}
		byte[] imageData = new byte[data.length * 2];
		// Log.i("whw", "*****************data.length="+data.length);
		for (int i = 0; i < data.length; i++) {
			imageData[i * 2] = (byte) (data[i] & 0xf0);
			imageData[i * 2 + 1] = (byte) (data[i] << 4 & 0xf0);
		}

		byte[] bmpData = toBmpByte(152,200, imageData);
		return bmpData;
	}


	private byte[] toBmpByte(int width, int height, byte[] data) {
		byte[] buffer = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);

			int bfType = 0x424d;
			int bfSize = 54 + 1024 + width * height;
			int bfReserved1 = 0;
			int bfReserved2 = 0;
			int bfOffBits = 54 + 1024;

			dos.writeShort(bfType);
			dos.write(changeByte(bfSize), 0, 4);
			dos.write(changeByte(bfReserved1), 0, 2);
			dos.write(changeByte(bfReserved2), 0, 2);
			dos.write(changeByte(bfOffBits), 0, 4);

			int biSize = 40;
			int biWidth = width;
			int biHeight = height;
			int biPlanes = 1;
			int biBitcount = 8;
			int biCompression = 0;
			int biSizeImage = width * height;
			int biXPelsPerMeter = 0;
			int biYPelsPerMeter = 0;
			int biClrUsed = 256;
			int biClrImportant = 0;

			dos.write(changeByte(biSize), 0, 4);
			dos.write(changeByte(biWidth), 0, 4);
			dos.write(changeByte(biHeight), 0, 4);
			dos.write(changeByte(biPlanes), 0, 2);
			dos.write(changeByte(biBitcount), 0, 2);
			dos.write(changeByte(biCompression), 0, 4);
			dos.write(changeByte(biSizeImage), 0, 4);
			dos.write(changeByte(biXPelsPerMeter), 0, 4);
			dos.write(changeByte(biYPelsPerMeter), 0, 4);
			dos.write(changeByte(biClrUsed), 0, 4);
			dos.write(changeByte(biClrImportant), 0, 4);


			byte[] palatte = new byte[1024];
			for (int i = 0; i < 256; i++) {
				palatte[i * 4] = (byte) i;
				palatte[i * 4 + 1] = (byte) i;
				palatte[i * 4 + 2] = (byte) i;
				palatte[i * 4 + 3] = 0;
			}
			dos.write(palatte);

			dos.write(data);

			dos.flush();
			buffer = baos.toByteArray();
			dos.close();
			// fos.close();
			baos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buffer;
	}


	private byte[] changeByte(int data) {
		byte b4 = (byte) ((data) >> 24);
		byte b3 = (byte) (((data) << 8) >> 24);
		byte b2 = (byte) (((data) << 16) >> 24);
		byte b1 = (byte) (((data) << 24) >> 24);
		byte[] bytes = { b1, b2, b3, b4 };
		return bytes;
	}

	private short getShort(byte b1, byte b2) {
		short temp = 0;
		temp |= (b1 & 0xff);
		temp <<= 8;
		temp |= (b2 & 0xff);
		return temp;
	}

	private byte[] short2byte(short s) {
		byte[] size = new byte[2];
		size[1] = (byte) (s & 0xff);
		size[0] = (byte) ((s >> 8) & 0xff);
		return size;
	}


	private boolean score(byte b1, byte b2) {
		byte[] temp = { b1, b2 };
		short score = 0;
		score |= (temp[0] & 0xff);
		score <<= 8;
		score |= (temp[1] & 0xff);
		Log.i("whw", "---------------score="+score);
		return score >= 50;
	}

	private void sendCommand(byte[] command) {
//		Log.i("whw", "sendCommand hex=" + DataUtils.toHexString(command));
		try {
			SerialPortManager.getInstance().write(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printlog(String tag, int length) {
		byte[] temp = new byte[length];
		System.arraycopy(buffer, 0, temp, 0, length);
		Log.i("whw", tag + "=" + DataUtils.toHexString(temp));
	}

}
