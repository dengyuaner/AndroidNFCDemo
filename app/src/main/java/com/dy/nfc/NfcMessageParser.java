package com.dy.nfc;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * 类名 NfcMessageParser
 * 作者 dy
 * 功能
 * 创建日期 2017/3/14 15:54
 * 修改日期 2017/3/14 15:54
 */


public class NfcMessageParser {

    private Intent tagIntent;
    private String TAG = "NfcMessageParser";

    public NfcMessageParser() {

    }

    public NfcMessageParser(Intent intent) {
        this.tagIntent = intent;
    }

    // 解析NFC信息，
    public List<String> getTagMessage() {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(tagIntent.getAction())) {
            NdefMessage[] msgs = getTagNdef(tagIntent);
            List<String> ndefList = getNdefString(msgs);

            if (ndefList != null && ndefList.size() != 0) {
                return ndefList;
            }
        }
        return null;
    }


    // 得到Intent中的NDEF数据
    private NdefMessage[] getTagNdef(Intent intent) {
        // TODO Auto-generated method stub
        NdefMessage[] msgs = null;
        Parcelable[] rawMsgs = intent
                .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        //把序列化数据转成Messaeg对象
        if (rawMsgs != null) {
            msgs = new NdefMessage[rawMsgs.length];
            for (int i = 0; i < rawMsgs.length; i++) {
                msgs[i] = (NdefMessage) rawMsgs[i];
            }
        } else {
            // Unknown tag type
            byte[] empty = new byte[]{};
            NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty,
                    empty, empty);
            NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
            msgs = new NdefMessage[]{msg};
        }
        return msgs;
    }

    // 把Message转成List
    private List<String> getNdefString(NdefMessage[] msgs) {
        // TODO Auto-generated method stub
        if (msgs != null && msgs.length != 0) {
            List<String> tagMessage = parser(msgs[0]);
            return tagMessage;
        }
        return null;
    }

    // 把NDEF中的信息系转化为Record，并最终转化为String
    private List<String> parser(NdefMessage ndefMessage) {
        // TODO Auto-generated method stub
        NdefRecord[] records = ndefMessage.getRecords();
        List<String> elements = new ArrayList<>();
        for (NdefRecord ndefRecord : records) {
            if (!TextRecord.isText(ndefRecord)) {
                return null;
            }
            elements.add(TextRecord.parse(ndefRecord));
        }
        return elements;
    }

    // 字符序列转换为16进制字符串
    private String bytesToHexString(byte[] src) {
        return bytesToHexString(src, true);
    }

    private String bytesToHexString(byte[] src, boolean isPrefix) {
        StringBuilder stringBuilder = new StringBuilder();
        if (isPrefix) {
            stringBuilder.append("0x");
        }
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.toUpperCase(Character.forDigit(
                    (src[i] >>> 4) & 0x0F, 16));
            buffer[1] = Character.toUpperCase(Character.forDigit(src[i] & 0x0F,
                    16));
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

}
