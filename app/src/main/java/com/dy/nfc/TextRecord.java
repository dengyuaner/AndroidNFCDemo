package com.dy.nfc;

import android.nfc.NdefRecord;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * 类名 TextRecord
 * 作者 dy
 * 功能 标签管理
 * 创建日期 2017/3/14 15:55
 * 修改日期 2017/3/14 15:55
 */


public class TextRecord {

    public static boolean isText(NdefRecord ndefRecord) {
        // TODO Auto-generated method stub
        try {
            parse(ndefRecord);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 吧Ndef记录转为String，这里的格式是TNF_WELL_KNOWN with RTD_TEXT
     * @param ndefRecord
     * @return
     */
    public static String parse(NdefRecord ndefRecord) {
        // TODO Auto-generated method stub
        if (ndefRecord.getTnf() != NdefRecord.TNF_WELL_KNOWN
                || !Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
            throw new IllegalArgumentException("NFC类型不正确...");
        }
        try {
            byte[] payload = ndefRecord.getPayload();
            //把byte转为String
            String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8"
                    : "UTF-16";
            int languageCodeLength = payload[0] & 0077;
            String languageCode = new String(payload, 1, languageCodeLength,
                    "US-ASCII");
            String text = new String(payload, languageCodeLength + 1,
                    payload.length - languageCodeLength - 1, textEncoding);
            return text;
        } catch (UnsupportedEncodingException e) {
            // should never happen unless we get a malformed tag.
            throw new IllegalArgumentException(e);
        }
    }

}
