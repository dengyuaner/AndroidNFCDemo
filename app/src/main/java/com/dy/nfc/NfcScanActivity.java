package com.dy.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class NfcScanActivity extends Activity {
    NfcAdapter mAdapter;
    TextView promt;
    NdefMessage mNdefPushMessage;
    PendingIntent mPendingIntent;
    String[][] techListsArray;
    private Button btnWrite;
    String datas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_scan);
        promt = (TextView) findViewById(R.id.promt);
        btnWrite = (Button) findViewById(R.id.btnWrite);
        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NfcScanActivity.this, NfcWriteTextActivity.class);
                if (datas != null)
                    intent.putExtra("data", datas);
                startActivity(intent);
            }
        });


        // 获取默认的NFC控制器
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
            promt.setText("设备不支持NFC！");
            finish();
            return;
        }
        if (!mAdapter.isEnabled()) {
            promt.setText("请在系统设置中先启用NFC功能！");
            finish();
            return;
        }
        mPendingIntent = PendingIntent
                .getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    public void onNewIntent(Intent paramIntent) {
        setIntent(paramIntent);
        resolveIntent(paramIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.mAdapter == null)
            return;
        if (!this.mAdapter.isEnabled()) {
            promt.setText("请在系统设置中先启用NFC功能！");
        }
        this.mAdapter.enableForegroundDispatch(this, this.mPendingIntent, null, null);
    }


    protected void resolveIntent(Intent intent) {

        // 得到是否检测到TAG触发
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            // 处理该intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            // 获取标签id数组
            byte[] bytesId = tag.getId();

            //获取消息内容
            NfcMessageParser nfcMessageParser = new NfcMessageParser(intent);
            List<String> tagMessage = nfcMessageParser.getTagMessage();

            if (tagMessage == null || tagMessage.size() == 0) {

                //Toast.makeText(this, "NFC格式不支持...", Toast.LENGTH_LONG).show();
            } else {
                for (int i = 0; i < tagMessage.size(); i++) {
                    Log.e("tag", tagMessage.get(i));
                }
                datas = tagMessage.get(0);
            }
            String info = "";
            if (datas != null) {
                info += "内容：" + datas + "\n卡片ID：" + bytesToHexString(bytesId) + "\n";
            } else {
                info += "内容：空" + "\n卡片ID：" + bytesToHexString(bytesId) + "\n";
            }


            String[] techList = tag.getTechList();

            //分析NFC卡的类型： Mifare Classic/UltraLight Info
            String cardType = "";


            for (String aTechList : techList) {
                if (TextUtils.equals(aTechList, "android.nfc.tech.Ndef")) {
                    Ndef ndef = Ndef.get(tag);
                    cardType += "最大数据尺寸:" + ndef.getMaxSize() + "字节";
                }
            }

            info += cardType;

            promt.setText("NFC信息如下：\n" + info);


        }
    }

    /**
     * 数组转换成十六进制字符串
     *
     * @param bArray
     * @return
     */
    public static String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }







}
