package com.onepay.onepaygo.tdynamo;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;


import com.magtek.mobile.android.mtlib.IMTCardData;
import com.magtek.mobile.android.mtlib.MTCardDataState;
import com.magtek.mobile.android.mtlib.MTConnectionState;
import com.magtek.mobile.android.mtlib.MTConnectionType;
import com.magtek.mobile.android.mtlib.MTDeviceConstants;
import com.magtek.mobile.android.mtlib.MTEMVEvent;
import com.magtek.mobile.android.mtlib.MTSCRA;
import com.magtek.mobile.android.mtlib.MTSCRAEvent;
import com.onepay.onepaygo.R;
import com.onepay.onepaygo.callback.CallbackInterface;
import com.onepay.onepaygo.common.Constants;
import com.onepay.onepaygo.common.Logger;
import com.onepay.onepaygo.common.PreferencesKeys;
import com.onepay.onepaygo.common.Utils;
import com.onepay.onepaygo.data.AppSharedPreferences;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;


public class TDynamoUtils implements CallbackInterface {
    private SharedPreferences sharedPreferences;
    static BluetoothAdapter mBluetoothAdapter;
    static boolean mScanning;
    static Handler mHandler;
    static UUID mServiceUuid;
    static CustomScanCallback mScanCallback = null;
    private static final long SCAN_PERIOD = 10000;
    static ArrayList<BluetoothDevice> foundDevicestdynamo = new ArrayList<>();
    private Context mcontext;
    private Activity mActivity;
    static AudioManager m_audioManager;
    static MTSCRA m_scra;
    Handler m_scraHandler = new Handler(new SCRAHandlerCallback());
//    PaymentExpandableListAdapter paymentExpandableListAdapter;
    public MTConnectionState m_connectionState = MTConnectionState.Disconnected;
    private boolean m_emvMessageFormatRequestPending;
    String arqcData, pos, serviceCode, transactionStatus;
    public TDynamoCallbackListener tDynamoCallbackListener;
    public TDynamoPaymentListener tDynamoPaymentListener;
    public static TDynamoUtils instance;
    private boolean isPaymentMode = false;

    public void setPaymentMode(boolean paymentMode) {
        isPaymentMode = paymentMode;
    }

    @Override
    public void onCallback(String msg) {
        tDynamoCallbackListener.onTDynamoFailure("");
    }

    public interface TDynamoCallbackListener {
        void onTDynamoSuccess(ArrayList<BluetoothDevice> foundDevicestdynamo);

        void onTDynamoFailure(String responseMsg);

    }
    public interface TDynamoPaymentListener {
        void onSuccess(String foundDevicestdynamo);
        void onUpdateStatus(String status);

        void onFailure(String responseMsg);

    }

    public void setCallbackListener(TDynamoCallbackListener callbackListener) {
        this.tDynamoCallbackListener = callbackListener;
    }
    public void setPaymentListener(TDynamoPaymentListener callbackListener) {
        this.tDynamoPaymentListener = callbackListener;
    }
    public void init(Context context,Activity activity) {
        sharedPreferences = AppSharedPreferences.Companion.getSharedPreferences(context);
        this.mcontext = context;
        this.mActivity = mActivity;
    }

    public static TDynamoUtils getInstance() {
        if (instance == null) {
            instance = new TDynamoUtils();
        }
        return instance;
    }

    @SuppressLint("MissingPermission")
    public void scanLeDevice(final boolean enable, Context context) {
        try {

            mcontext = context;
            m_scra = new MTSCRA(context, m_scraHandler);
            mHandler = new Handler();
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
                return;
            }

            if (enable) {
                stopScanning();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopScanning();

                    }
                }, SCAN_PERIOD);

                mScanning = true;

                if (Build.VERSION.SDK_INT >= 21) {
                    BluetoothLeScanner leScanner = mBluetoothAdapter.getBluetoothLeScanner();

                    if (leScanner != null) {
                        if (mScanCallback == null) {
                            mScanCallback = new CustomScanCallback();
                        }

                        leScanner.startScan(mScanCallback);
                    }
                } else {
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                }

                mServiceUuid = MTDeviceConstants.UUID_SCRA_BLE_EMV_T_DEVICE_READER_SERVICE;

            } else {
                stopScanning();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addDevice(BluetoothDevice device) {
        try {
            foundDevicestdynamo = new ArrayList<>();
            if (!foundDevicestdynamo.contains(device)) {
                foundDevicestdynamo.add(device);
                if (tDynamoCallbackListener != null)
                    tDynamoCallbackListener.onTDynamoSuccess(foundDevicestdynamo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    public void stopScanning() {
        if (mScanning) {
            if (mBluetoothAdapter != null) {

                mBluetoothAdapter.cancelDiscovery();

                if (Build.VERSION.SDK_INT >= 21) {
                    BluetoothLeScanner leScanner = mBluetoothAdapter.getBluetoothLeScanner();

                    if ((leScanner != null) && (mScanCallback != null)) {
                        leScanner.stopScan(mScanCallback);
                    }
                } else {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }

            }
        }

        mScanning = false;

    }

    public long openDevice(Context context) {

        MTConnectionType m_connectionType = MTConnectionType.BLEEMVT;
        m_audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        String m_deviceAddress = foundDevicestdynamo.get(0).getAddress();

        long result = -1;

        if (m_scra != null) {
            m_scra.setConnectionType(m_connectionType);
            m_scra.setAddress(m_deviceAddress);

            boolean enableRetry = true;

            m_scra.setConnectionRetry(enableRetry);

            m_scra.openDevice();

            result = 0;
        }

        return result;
    }

    public List<UUID> parseUUIDs(final byte[] advertisedData) {
        List<UUID> uuids = new ArrayList<UUID>();

        int offset = 0;
        while (offset < (advertisedData.length - 2)) {
            int len = advertisedData[offset++];
            if (len == 0)
                break;

            int type = advertisedData[offset++];
            switch (type) {
                case 0x06:// Partial list of 128-bit UUIDs
                case 0x07:// Complete list of 128-bit UUIDs
                    // Loop through the advertised 128-bit UUID's.
                    while (len >= 16) {
                        try {
                            // Wrap the advertised bits and order them.
                            ByteBuffer buffer = ByteBuffer.wrap(advertisedData,
                                    offset++, 16).order(ByteOrder.LITTLE_ENDIAN);
                            long mostSignificantBit = buffer.getLong();
                            long leastSignificantBit = buffer.getLong();
                            uuids.add(new UUID(leastSignificantBit,
                                    mostSignificantBit));
                        } catch (IndexOutOfBoundsException e) {
                            continue;
                        } finally {
                            offset += 15;
                            len -= 16;
                        }
                    }
                    break;
                default:
                    offset += (len - 1);
                    break;
            }
        }

        return uuids;
    }


    public BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            boolean found = false;

            if (scanRecord != null) {
                List<UUID> uuidList = parseUUIDs(scanRecord);

                ListIterator<UUID> uuidListIt = uuidList.listIterator();

                while (uuidListIt.hasNext()) {
                    UUID scanUuid = uuidListIt.next();

                    if (scanUuid.compareTo(mServiceUuid) == 0) {
                        found = true;
                    }
                }
            }

            if (found) {

            }
        }
    };


    public class SCRAHandlerCallback implements Handler.Callback {

        private boolean[] mTypeChecked = new boolean[]{true, true, false};
        int m_emvMessageFormat = 0;
        private Object m_syncEvent = null;
        private String m_syncData = "";
        private boolean m_turnOffLEDPending;


        public boolean handleMessage(Message msg) {
            try {

                switch (msg.what) {
                    case MTSCRAEvent.OnDeviceConnectionStateChanged:
                        OnDeviceStateChanged((MTConnectionState) msg.obj);
                        break;
                    case MTSCRAEvent.OnCardDataStateChanged:
                        OnCardDataStateChanged((MTCardDataState) msg.obj);
                        break;
                    case MTSCRAEvent.OnDataReceived:
                        OnCardDataReceived((IMTCardData) msg.obj);
                        break;
                    case MTSCRAEvent.OnDeviceResponse:
                        OnDeviceResponse((String) msg.obj);
                        break;
                    case MTEMVEvent.OnTransactionStatus:
                        OnTransactionStatus((byte[]) msg.obj);
                        break;
                    case MTEMVEvent.OnDisplayMessageRequest:
                        OnDisplayMessageRequest((byte[]) msg.obj);
                        break;
                    case MTEMVEvent.OnARQCReceived:
                        OnARQCReceived((byte[]) msg.obj);
                        break;
                    case MTEMVEvent.OnTransactionResult:
                        OnTransactionResult((byte[]) msg.obj);
                        break;

                    case MTEMVEvent.OnEMVCommandResult:
                        OnEMVCommandResult((byte[]) msg.obj);
                        break;

                }
            } catch (Exception ex) {
                Logger.error(" SCRAHandlerCallback implements Handler.Callback {", ex.getMessage());
            }

            return true;
        }

        public void OnDeviceStateChanged(MTConnectionState deviceState) {
            setState(deviceState);


            switch (deviceState) {
                case Disconnected:
                    AppSharedPreferences.Companion.writeSp(sharedPreferences,PreferencesKeys.deviceStatus, false);
                    if(tDynamoPaymentListener != null) tDynamoPaymentListener.onFailure("Disconnected");
//                    paymentExpandableListAdapter = new PaymentExpandableListAdapter(mcontext, null, null);
//                    paymentExpandableListAdapter.setVisibilityListDevice(-1);
                    break;

                case Connected:
                    if (isPaymentMode) {
                        requestEMVMessageFormat();
                        return;
                    }
                    AppSharedPreferences.Companion.writeSp(sharedPreferences, PreferencesKeys.deviceStatus, true);
                    AppSharedPreferences.Companion.writeSp(sharedPreferences, PreferencesKeys.selectedDevice, Constants.DeviceType.MAGTEK.name());
                    if (foundDevicestdynamo.size() > 0)
                        AppSharedPreferences.Companion.writeSp(sharedPreferences, PreferencesKeys.bluetoothAddress, foundDevicestdynamo.get(0).getAddress());
                    HashMap<String, String> terminalId_retail = new HashMap<String, String>();
                    terminalId_retail.put("1", "1");
//                    paymentExpandableListAdapter = new PaymentExpandableListAdapter(mcontext, null, null);
//                    paymentExpandableListAdapter.setVisibilityListDevice(Integer.parseInt(AppSharedPreferences.Companion.readString(sharedPreferences, PreferencesKeys.tickPosdevice)));
                 if(tDynamoPaymentListener == null)  Utils.Companion.openDialogVoid(mcontext, "Connected to tDynamo", "", TDynamoUtils.this);
                    break;
                case Error:
                    break;
                case Connecting:
                    if (isPaymentMode) return;
                    Toast.makeText(mcontext, "connecting", Toast.LENGTH_LONG).show();

                    break;
                case Disconnecting:
                    Toast.makeText(mcontext, "disconnecting", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        public void connectDevice() {
            if (m_scra == null)
                m_scra = new MTSCRA(mcontext, m_scraHandler);
            MTConnectionType m_connectionType = MTConnectionType.BLEEMVT;
            m_audioManager = (AudioManager) mcontext.getSystemService(Context.AUDIO_SERVICE);
            String m_deviceAddress = AppSharedPreferences.Companion.readString(sharedPreferences,PreferencesKeys.bluetoothAddress);
            if (m_scra != null) {
                m_scra.setConnectionType(m_connectionType);
                m_scra.setAddress(m_deviceAddress);
                if (m_scra.isDeviceConnected())
                    requestEMVMessageFormat();
                else
                    m_scra.openDevice();
            }

        }

        public void requestEMVMessageFormat() {
            if (m_scra == null) {
                m_scra = new MTSCRA(mcontext, m_scraHandler);
                connectDevice();
            }
            sendSetDateTimeCommand();
            m_emvMessageFormatRequestPending = true;

            int status = sendCommand("000168");

            if (status != MTSCRA.SEND_COMMAND_SUCCESS) {
                m_emvMessageFormatRequestPending = false;
            }
            setMSRPower(true);
            startTransactionWithOptions();
        }

        private void sendSetDateTimeCommand() {
            Calendar now = Calendar.getInstance();

            int month = now.get(Calendar.MONTH) + 1;
            int day = now.get(Calendar.DAY_OF_MONTH);
            int hour = now.get(Calendar.HOUR_OF_DAY);
            int minute = now.get(Calendar.MINUTE);
            int second = now.get(Calendar.SECOND);
            int year = now.get(Calendar.YEAR) - 2008;

            String dateTimeString = String.format("%1$02x%2$02x%3$02x%4$02x%5$02x00%6$02x", month, day, hour, minute, second, year);
            String command = new String("49220000030C001C0000000000000000000000000000000000") + dateTimeString + "00000000";

            sendCommand(command);
        }


        public int sendCommand(String command) {

            int result = MTSCRA.SEND_COMMAND_ERROR;
            if (m_scra != null) {
                result = m_scra.sendCommandToDevice(command);
            }
            return result;
        }


        protected void OnCardDataStateChanged(MTCardDataState cardDataState) {
            switch (cardDataState) {
                case DataNotReady:

                    break;
                case DataReady:

                    break;
                case DataError:

                    break;
            }

        }

        protected void OnCardDataReceived(IMTCardData cardData) {
            m_scra.getResponseData();
            getCardInfo();
            cardData.getTLVPayload();

        }

        protected void notifySyncData(String data) {
            if (m_syncEvent != null) {
                synchronized (m_syncEvent) {
                    m_syncData = data;
                    m_syncEvent.notifyAll();
                }
            }
        }

        private void setMSRPower(boolean state) {
            String command = "5801" + (state ? "01" : "00");

            sendCommand(command);
        }


        protected void OnDeviceResponse(String data) {

            notifySyncData(data);


            if (m_emvMessageFormatRequestPending) {
                m_emvMessageFormatRequestPending = false;

                byte[] emvMessageFormatResponseByteArray = TLVParser.getByteArrayFromHexString(data);

                if (emvMessageFormatResponseByteArray.length == 3) {
                    if ((emvMessageFormatResponseByteArray[0] == 0) && (emvMessageFormatResponseByteArray[1] == 1)) {
                        m_emvMessageFormat = emvMessageFormatResponseByteArray[2];
                    }
                }
            }

        }


        public void startTransaction() {
            byte type = 0;

            if (mTypeChecked[0]) {
                type |= (byte) 0x01;
            }

            if (mTypeChecked[1]) {
                type |= (byte) 0x02;
            }

            if (mTypeChecked[2]) {
                type |= (byte) 0x04;
            }

        }

        public void startTransactionWithOptions() {
            String actualAmount = "0.0";//SaleFragment.amountSetting.replace(".", "");
            String convertedAmount = "";
            for (int i = 0; i < 12 - actualAmount.length(); i++) {
                String newAmount = convertedAmount.concat("0");
                convertedAmount = newAmount;

            }
            String finalAmount = convertedAmount.concat(actualAmount);
            if (m_scra != null) {

                byte timeLimit = 0x3C;
//         j       byte timeLimit = 0x09;
//                byte cardType = 0x02;  // Chip Only
                //  byte cardType = 0x03;  // MSR + Chip
                byte cardType = 0x07;  // MSR + Chip
                byte option = (isQuickChipEnabled() ? (byte) 0x80 : 00);
                // byte[] amount = new byte[]{0x00, 0x00, 0x00, 0x00, 0x15, 0x00};
                byte[] amount = TLVParser.getByteArrayFromHexString(finalAmount);
                //byte[] amount =hexStringToByte(SaleFragment.amountSetting);
                byte transactionType = 0x00; // Purchase
                byte[] cashBack = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
                byte[] currencyCode = new byte[]{0x08, 0x40};
                byte reportingOption = 0x02;  // All Status Changes


                int result = m_scra.startTransaction(timeLimit, cardType, option, amount, transactionType, cashBack, currencyCode, reportingOption);//transaction type 0 swipe//9, 3, 0, [0,0,0,0,21,0], 0, [0,0,0,0,0,0], [8,64], 2

                Logger.debug("startTransactionWithOptions(byte cardType) ", String.valueOf(result));


            } else {
                Utils.Companion.openDialogDevice(mcontext,mActivity);

            }
        }


        protected boolean isQuickChipEnabled() {
            boolean enabled = false;


            return enabled;
        }


        protected void OnARQCReceived(byte[] data) {

            if (isQuickChipEnabled()) {

                return;
            }

            List<HashMap<String, String>> parsedTLVList = TLVParser.parseEMVData(data, true, "");

            if (parsedTLVList != null) {
                String macKSNString = TLVParser.getTagValue(parsedTLVList, "DFDF54");
                byte[] macKSN = TLVParser.getByteArrayFromHexString(macKSNString);

                String macEncryptionTypeString = TLVParser.getTagValue(parsedTLVList, "DFDF55");
                byte[] macEncryptionType = TLVParser.getByteArrayFromHexString(macEncryptionTypeString);

                String deviceSNString = TLVParser.getTagValue(parsedTLVList, "DFDF25");
                byte[] deviceSN = TLVParser.getByteArrayFromHexString(deviceSNString);


                boolean approved = isTransactionApproved(data);

                byte[] response = null;

                if (m_emvMessageFormat == 0) {
                    response = buildAcquirerResponseFormat0(deviceSN, approved);
                } else if (m_emvMessageFormat == 1) {
                    response = buildAcquirerResponseFormat1(macKSN, macEncryptionType, deviceSN, approved);
                }

                setAcquirerResponse(response);

                Logger.debug(" OnARQCReceived(byte[] data)", TLVParser.getHexString(data));
                arqcData = TLVParser.getHexString(data);

            }
        }

        protected boolean isTransactionApproved(byte[] data) {
            boolean approved = true;
            return approved;
        }

        public void setAcquirerResponse(byte[] response) {
            if ((m_scra != null) && (response != null)) {

                Logger.debug("setAcquirerResponse(byte[] response)", TLVParser.getHexString(response));


                m_scra.setAcquirerResponse(response);
            }
        }

        public void cancelTransaction() {
            if (m_scra != null) {
                m_turnOffLEDPending = true;

                int result = m_scra.cancelTransaction();

                //  sendToDisplay("[Cancel Transaction] (Result=" + result + ")");
            }
        }

        protected byte[] buildAcquirerResponseFormat0(byte[] deviceSN, boolean approved) {
            byte[] response = null;

            int lenSN = 0;

            if (deviceSN != null)
                lenSN = deviceSN.length;

            byte[] snTag = new byte[]{(byte) 0xDF, (byte) 0xDF, 0x25, (byte) lenSN};
            byte[] container = new byte[]{(byte) 0xFA, 0x06, 0x70, 0x04};
            byte[] approvedARC = new byte[]{(byte) 0x8A, 0x02, 0x30, 0x30};
            byte[] declinedARC = new byte[]{(byte) 0x8A, 0x02, 0x30, 0x35};

            int len = 4 + snTag.length + lenSN + container.length + approvedARC.length;

            response = new byte[len];

            int i = 0;
            len -= 2;
            response[i++] = (byte) ((len >> 8) & 0xFF);
            response[i++] = (byte) (len & 0xFF);
            len -= 2;
            response[i++] = (byte) 0xF9;
            response[i++] = (byte) len;
            System.arraycopy(snTag, 0, response, i, snTag.length);
            i += snTag.length;
            System.arraycopy(deviceSN, 0, response, i, deviceSN.length);
            i += deviceSN.length;
            System.arraycopy(container, 0, response, i, container.length);
            i += container.length;
            if (approved) {
                System.arraycopy(approvedARC, 0, response, i, approvedARC.length);
            } else {
                System.arraycopy(declinedARC, 0, response, i, declinedARC.length);
            }

            return response;
        }

        protected byte[] buildAcquirerResponseFormat1(byte[] macKSN, byte[] macEncryptionType, byte[] deviceSN, boolean approved) {
            byte[] response = null;

            int lenMACKSN = 0;
            int lenMACEncryptionType = 0;
            int lenSN = 0;

            if (macKSN != null) {
                lenMACKSN = macKSN.length;
            }

            if (macEncryptionType != null) {
                lenMACEncryptionType = macEncryptionType.length;
            }

            if (deviceSN != null) {
                lenSN = deviceSN.length;
            }

            byte[] macKSNTag = new byte[]{(byte) 0xDF, (byte) 0xDF, 0x54, (byte) lenMACKSN};
            byte[] macEncryptionTypeTag = new byte[]{(byte) 0xDF, (byte) 0xDF, 0x55, (byte) lenMACEncryptionType};
            byte[] snTag = new byte[]{(byte) 0xDF, (byte) 0xDF, 0x25, (byte) lenSN};
            byte[] container = new byte[]{(byte) 0xFA, 0x06, 0x70, 0x04};
            byte[] approvedARC = new byte[]{(byte) 0x8A, 0x02, 0x30, 0x30};
            byte[] declinedARC = new byte[]{(byte) 0x8A, 0x02, 0x30, 0x35};

            int lenTLV = 4 + macKSNTag.length + lenMACKSN + macEncryptionTypeTag.length + lenMACEncryptionType + snTag.length + lenSN + container.length + approvedARC.length;

            int lenPadding = 0;

            if ((lenTLV % 8) > 0) {
                lenPadding = (8 - lenTLV % 8);
            }

            int lenData = lenTLV + lenPadding + 4;

            response = new byte[lenData];

            int i = 0;
            response[i++] = (byte) (((lenData - 2) >> 8) & 0xFF);
            response[i++] = (byte) ((lenData - 2) & 0xFF);
            response[i++] = (byte) 0xF9;
            response[i++] = (byte) (lenTLV - 4);
            System.arraycopy(macKSNTag, 0, response, i, macKSNTag.length);
            i += macKSNTag.length;
            System.arraycopy(macKSN, 0, response, i, macKSN.length);
            i += macKSN.length;
            System.arraycopy(macEncryptionTypeTag, 0, response, i, macEncryptionTypeTag.length);
            i += macEncryptionTypeTag.length;
            System.arraycopy(macEncryptionType, 0, response, i, macEncryptionType.length);
            i += macEncryptionType.length;
            System.arraycopy(snTag, 0, response, i, snTag.length);
            i += snTag.length;
            System.arraycopy(deviceSN, 0, response, i, deviceSN.length);
            i += deviceSN.length;
            System.arraycopy(container, 0, response, i, container.length);
            i += container.length;

            if (approved) {
                System.arraycopy(approvedARC, 0, response, i, approvedARC.length);
            } else {
                System.arraycopy(declinedARC, 0, response, i, declinedARC.length);
            }

            return response;
        }

        protected void OnTransactionResult(byte[] data) {
            if (data != null) {
                if (data.length > 0) {
                    boolean signatureRequired = (data[0] != 0);

                    int lenBatchData = data.length - 3;
                    if (lenBatchData > 0) {
                        byte[] batchData = new byte[lenBatchData];

                        System.arraycopy(data, 3, batchData, 0, lenBatchData);

                        // sendToDisplay("(Parsed Batch Data)");
                        Logger.debug("OnTransactionResult(byte[] data)", "(Parsed Batch Data)");


                        List<HashMap<String, String>> parsedTLVList = TLVParser.parseEMVData(batchData, false, "");

                        displayParsedTLV(parsedTLVList);

                        String cidString = TLVParser.getTagValue(parsedTLVList, "9F27");
                        pos = TLVParser.getTagValue(parsedTLVList, "9F39");
                        byte[] cidValue = TLVParser.getByteArrayFromHexString(cidString);

                        if (transactionStatus.equals("APPROVED")) {

       //                     CardExpandableListAdapter.btn_start.setVisibility(View.VISIBLE);
       //                     CardExpandableListAdapter.btn_start.setText("****************");
                            AppSharedPreferences.Companion.writeSp(sharedPreferences, PreferencesKeys.arqc, arqcData);
                            AppSharedPreferences.Companion.writeSp(sharedPreferences, PreferencesKeys.pos, pos);
                            AppSharedPreferences.Companion.writeSp(sharedPreferences, PreferencesKeys.serviceCode, serviceCode);
                            AppSharedPreferences.Companion.writeSp(sharedPreferences,  PreferencesKeys.deviceCode, "MAGTEK");
                            if(tDynamoPaymentListener!=null)
                                tDynamoPaymentListener.onSuccess("****************");
                        } else if (transactionStatus.equals("DECLINED")) {
                            cancelTransaction();
                            Utils.Companion.openDialogVoid(mcontext, mcontext.getResources().getString(R.string.payment_failed), "",null);
                            if(tDynamoPaymentListener!=null)
                                tDynamoPaymentListener.onFailure("failed");
                        }

                        boolean approved = false;

                        if (cidValue != null) {
                            if (cidValue.length > 0) {
                                if ((cidValue[0] & (byte) 0x40) != 0) {
                                    approved = true;
                                }
                            }
                        }

                        if (approved) {
                            if (signatureRequired) {
                                Logger.debug(" OnTransactionResult(byte[] data)", "signature");
                            } else {
                            }
                        }
                    }
                }
            }

        }

        private void displayParsedTLV(List<HashMap<String, String>> parsedTLVList) {
            if (parsedTLVList != null) {
                ListIterator<HashMap<String, String>> it = parsedTLVList.listIterator();

                while (it.hasNext()) {
                    HashMap<String, String> map = it.next();
                    String tagString = map.get("tag");
                    String valueString = map.get("value");
                    Logger.debug("parsed batch data", tagString + "=" + valueString);
                }
            }
        }

        protected void OnEMVCommandResult(byte[] data) {
            Logger.debug(" OnEMVCommandResult(byte[] data)", TLVParser.getHexString(data));
            if (m_turnOffLEDPending) {
                m_turnOffLEDPending = false;

            }
        }

        protected void OnDisplayMessageRequest(byte[] data) {
            transactionStatus = TLVParser.getTextString(data, 0);
//            CardExpandableListAdapter.layout_anim.setVisibility(View.GONE);
//            CardExpandableListAdapter.btn_start.setVisibility(View.VISIBLE);
//            CardExpandableListAdapter.btn_start.setText(transactionStatus);
            if(tDynamoPaymentListener != null)
                tDynamoPaymentListener.onUpdateStatus(transactionStatus);
        }

        protected void OnTransactionStatus(byte[] data) {
            Logger.debug("OnTransactionStatus", TLVParser.getHexString(data));
        }


        public String getCardInfo() {
            try {
                StringBuilder stringBuilder = new StringBuilder();

                stringBuilder.append(String.format("Tracks.Masked=%s \n", m_scra.getMaskedTracks()));

                stringBuilder.append(String.format("Track1.Encrypted=%s \n", m_scra.getTrack1()));
                stringBuilder.append(String.format("Track2.Encrypted=%s \n", m_scra.getTrack2()));
                stringBuilder.append(String.format("Track3.Encrypted=%s \n", m_scra.getTrack3()));

                stringBuilder.append(String.format("Track1.Masked=%s \n", m_scra.getTrack1Masked()));
                stringBuilder.append(String.format("Track2.Masked=%s \n", m_scra.getTrack2Masked()));
                stringBuilder.append(String.format("Track3.Masked=%s \n", m_scra.getTrack3Masked()));

                stringBuilder.append(String.format("MagnePrint.Encrypted=%s \n", m_scra.getMagnePrint()));
                stringBuilder.append(String.format("MagnePrint.Status=%s \n", m_scra.getMagnePrintStatus()));
                stringBuilder.append(String.format("Device.Serial=%s \n", m_scra.getDeviceSerial()));
                stringBuilder.append(String.format("Session.ID=%s \n", m_scra.getSessionID()));
                stringBuilder.append(String.format("KSN=%s \n", m_scra.getKSN()));
                stringBuilder.append(formatStringIfNotEmpty("Cap.MagnePrint=%s \n", m_scra.getCapMagnePrint()));
                stringBuilder.append(formatStringIfNotEmpty("Cap.MagnePrintEncryption=%s \n", m_scra.getCapMagnePrintEncryption()));
                stringBuilder.append(formatStringIfNotEmpty("Cap.MagneSafe20Encryption=%s \n", m_scra.getCapMagneSafe20Encryption()));
                stringBuilder.append(formatStringIfNotEmpty("Cap.MSR=%s \n", m_scra.getCapMSR()));
                stringBuilder.append(formatStringIfNotEmpty("Cap.Tracks=%s \n", m_scra.getCapTracks()));

                stringBuilder.append(String.format("Card.Data.CRC=%d \n", m_scra.getCardDataCRC()));
                stringBuilder.append(String.format("Card.Exp.Date=%s \n", m_scra.getCardExpDate()));
                stringBuilder.append(String.format("Card.IIN=%s \n", m_scra.getCardIIN()));
                stringBuilder.append(String.format("Card.Last4=%s \n", m_scra.getCardLast4()));
                stringBuilder.append(String.format("Card.Name=%s \n", m_scra.getCardName()));
                stringBuilder.append(String.format("Card.PAN=%s \n", m_scra.getCardPAN()));
                stringBuilder.append(String.format("Card.PAN.Length=%d \n", m_scra.getCardPANLength()));
                stringBuilder.append(String.format("Card.Service.Code=%s \n", m_scra.getCardServiceCode()));
                stringBuilder.append(String.format("Card.Status=%s \n", m_scra.getCardStatus()));

                AppSharedPreferences.Companion.writeSp(sharedPreferences,  PreferencesKeys.track1, m_scra.getTrack1());
                AppSharedPreferences.Companion.writeSp(sharedPreferences,  PreferencesKeys.track2, m_scra.getTrack2());
                AppSharedPreferences.Companion.writeSp(sharedPreferences,  PreferencesKeys.track3, m_scra.getTrack3());
                AppSharedPreferences.Companion.writeSp(sharedPreferences,  PreferencesKeys.ksn, m_scra.getKSN());
                AppSharedPreferences.Companion.writeSp(sharedPreferences,  PreferencesKeys.maskedpan, "************" + m_scra.getCardLast4());
                AppSharedPreferences.Companion.writeSp(sharedPreferences,  PreferencesKeys.deviceCode, "MAGTEK");
//                CardExpandableListAdapter.layout_anim.setVisibility(View.GONE);
//                CardExpandableListAdapter.btn_start.setVisibility(View.VISIBLE);
//                CardExpandableListAdapter.btn_start.setText(Util.readFromPreferences(mcontext, PreferencesKeys.maskedpan, ""));
                if(tDynamoPaymentListener != null) tDynamoPaymentListener.onSuccess("APPROVED_TO");


                stringBuilder.append(formatStringIfNotEmpty("HashCode=%s \n", m_scra.getHashCode()));
                stringBuilder.append(formatStringIfNotValueZero("Data.Field.Count=%s \n", m_scra.getDataFieldCount()));

                stringBuilder.append(String.format("Encryption.Status=%s \n", m_scra.getEncryptionStatus()));
                stringBuilder.append(formatStringIfNotEmpty("MagTek.Device.Serial=%s \n", m_scra.getMagTekDeviceSerial()));

                stringBuilder.append(formatStringIfNotEmpty("Response.Type=%s \n", m_scra.getResponseType()));
                stringBuilder.append(formatStringIfNotEmpty("TLV.Version=%s \n", m_scra.getTLVVersion()));

                stringBuilder.append(String.format("Track.Decode.Status=%s \n", m_scra.getTrackDecodeStatus()));

                String tkStatus = m_scra.getTrackDecodeStatus();

                String tk1Status = "01";
                String tk2Status = "01";
                String tk3Status = "01";

                if (tkStatus.length() >= 6) {
                    tk1Status = tkStatus.substring(0, 2);
                    tk2Status = tkStatus.substring(2, 4);
                    tk3Status = tkStatus.substring(4, 6);

                    stringBuilder.append(String.format("Track1.Status=%s \n", tk1Status));
                    stringBuilder.append(String.format("Track2.Status=%s \n", tk2Status));
                    stringBuilder.append(String.format("Track3.Status=%s \n", tk3Status));
                }

                stringBuilder.append(String.format("SDK.Version=%s \n", m_scra.getSDKVersion()));

                stringBuilder.append(String.format("Battery.Level=%d \n", m_scra.getBatteryLevel()));

                return stringBuilder.toString();
            } catch (Exception e) {
                Logger.error(" getCardInfo() {", e.getMessage());
            }
            return "";
        }

        public String formatStringIfNotEmpty(String format, String data) {
            String result = "";

            if (!data.isEmpty()) {
                result = String.format(format, data);
            }

            return result;
        }

        public String formatStringIfNotValueZero(String format, int data) {
            String result = "";

            if (data != 0) {
                result = String.format(format, data);
            }

            return result;
        }


        public void setState(MTConnectionState deviceState) {
            m_connectionState = deviceState;


        }


    }
}
