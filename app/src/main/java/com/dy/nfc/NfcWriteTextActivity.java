package com.dy.nfc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

/**
 * 写入标签
 */

public class NfcWriteTextActivity extends Activity implements View.OnClickListener {
    private IntentFilter[] mWriteTagFilters;
    private NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    String[][] mTechLists;
    Button writeBtn1, writeBtn2;
    boolean isWrite = false;
    //写入框
    EditText mContentEditText;

    //nfc设备里已有的数据
    private String data;
    //提示框
    private AlertDialog mAlertDialog;

    private int tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfcwrite);

        data = getIntent().getStringExtra("data");
        writeBtn1 = (Button) findViewById(R.id.writeBtn1);
        writeBtn1.setOnClickListener(this);
        writeBtn2 = (Button) findViewById(R.id.writeBtn2);
        writeBtn2.setOnClickListener(this);

        mContentEditText = (EditText) findViewById(R.id.content_edit);

        // 获取nfc适配器，判断设备是否支持NFC功能
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            showToast("设备不支持NFC！");
            finish();
            return;
        } else if (!nfcAdapter.isEnabled()) {
            showToast("请在系统设置中先启用NFC功能！");
            finish();
            return;
        }
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // 写入标签权限
        IntentFilter writeFilter = new IntentFilter(
                NfcAdapter.ACTION_TECH_DISCOVERED);
        mWriteTagFilters = new IntentFilter[]{writeFilter};
        mTechLists = new String[][]{
                new String[]{MifareClassic.class.getName()},
                new String[]{NfcA.class.getName()}};// 允许扫描的标签类型

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.writeBtn1) {
            tag = 1;
            isWrite = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(NfcWriteTextActivity.this)
                    .setTitle("请将标签靠近！");
            builder.setNegativeButton("确定",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            dialog.dismiss();
                            mContentEditText.setText("");
                            isWrite = false;
                            NfcWriteTextActivity.this.finish();
                        }
                    });
            builder.setPositiveButton("取消",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            dialog.dismiss();
                            isWrite = false;
                        }
                    });
            mAlertDialog = builder.create();
            mAlertDialog.show();
        }
        if (v.getId() == R.id.writeBtn2) {
            tag = 2;
            isWrite = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(NfcWriteTextActivity.this)
                    .setTitle("请将标签靠近！");
            builder.setNegativeButton("确定",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            dialog.dismiss();
                            mContentEditText.setText("");
                            isWrite = false;
                            NfcWriteTextActivity.this.finish();
                        }
                    });
            builder.setPositiveButton("取消",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            dialog.dismiss();
                            isWrite = false;
                        }
                    });
            mAlertDialog = builder.create();
            mAlertDialog.show();
        }
    }


    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent,
                mWriteTagFilters, mTechLists);
    }

    // 写入模式时，才执行写入操作
    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        if (isWrite && NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NdefMessage ndefMessage = getNoteAsNdef();
            if (ndefMessage != null) {
                writeTag(getNoteAsNdef(), tag);
            } else {
                showToast("请输入您要写入标签的内容");
            }
        }
    }

    // 根据文本生成一个NdefRecord
    private NdefMessage getNoteAsNdef() {
        //拼接已有的数据和新写入的数据，如果想覆盖以前的数据，则不拼接data
        //同理，如果想格式化数据，则写入text为空即可
        String text = "";
        if (tag == 1) {
            if (!TextUtils.isEmpty(data)) {
                text = data + "\n" + mContentEditText.getText().toString();
            } else {
                text = mContentEditText.getText().toString();
            }
        }
        Log.e("text", text);
        NdefRecord textRecord = createTextRecord(text, Locale.getDefault(), false);
        return new NdefMessage(new NdefRecord[]{textRecord});

    }

    /**
     * 创建record，格式为TNF_WELL_KNOWN with RTD_TEXT
     *
     * @param payload      你要写入的数据
     * @param locale
     * @param encodeInUtf8 编码
     * @return
     */
    public NdefRecord createTextRecord(String payload, Locale locale, boolean encodeInUtf8) {

        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));

        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");

        byte[] textBytes = payload.getBytes(utfEncoding);

        int utfBit = encodeInUtf8 ? 0 : (1 << 7);

        char status = (char) (utfBit + langBytes.length);

        byte[] data = new byte[1 + langBytes.length + textBytes.length];

        data[0] = (byte) status;

        System.arraycopy(langBytes, 0, data, 1, langBytes.length);

        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);

        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,

                NdefRecord.RTD_TEXT, new byte[0], data);

        return record;

    }

    /**
     * 写入数据
     * @param message
     * @param tag  intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
     * @return
     */
    boolean writeTag(NdefMessage message, Tag tag) {

        int size = message.toByteArray().length;

        try {

            //链接nfc
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();

                if (!ndef.isWritable()) {
                    showToast("tag不允许写入");
                    return false;
                }
                if (ndef.getMaxSize() < size) {
                    showToast("文件大小超出容量");
                    return false;
                }

                ndef.writeNdefMessage(message);
                showToast("写入数据成功.");
                mAlertDialog.dismiss();
                mContentEditText.setText("");
                isWrite = false;
                finish();
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        showToast("格式化tag并且写入message");
                        return true;
                    } catch (IOException e) {
                        showToast("格式化tag失败.");
                        return false;
                    }
                } else {
                    showToast("Tag不支持NDEF");
                    return false;
                }
            }
        } catch (Exception e) {
            showToast("写入数据失败");
        }

        return false;
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
