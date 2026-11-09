package com.example.love_pet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;

public class WeChatShareManager {

    private static final String APP_ID = "wx1234567890abcdef";
    private static final int THUMB_SIZE = 150;

    private IWXAPI api;
    private Context context;

    public WeChatShareManager(Context context) {
        this.context = context;
        api = WXAPIFactory.createWXAPI(context, APP_ID, true);
        api.registerApp(APP_ID);
    }

    public boolean isWeChatInstalled() {
        return api.isWXAppInstalled();
    }

    public void shareWebPage(String title, String description, String url, int thumbResId, int scene) {
        if (!isWeChatInstalled()) {
            Toast.makeText(context, "请先安装微信", Toast.LENGTH_SHORT).show();
            return;
        }

        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;

        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = description;

        Bitmap thumb = BitmapFactory.decodeResource(context.getResources(), thumbResId);
        if (thumb != null) {
            Bitmap thumbBmp = Bitmap.createScaledBitmap(thumb, THUMB_SIZE, THUMB_SIZE, true);
            thumb.recycle();
            msg.thumbData = bmpToByteArray(thumbBmp, true);
        }

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = scene;

        api.sendReq(req);
    }

    public void shareWebPageToFriend(String title, String description, String url, int thumbResId) {
        shareWebPage(title, description, url, thumbResId, SendMessageToWX.Req.WXSceneSession);
    }

    public void shareWebPageToMoments(String title, String description, String url, int thumbResId) {
        shareWebPage(title, description, url, thumbResId, SendMessageToWX.Req.WXSceneTimeline);
    }

    public void shareImage(Bitmap bitmap, int scene) {
        if (!isWeChatInstalled()) {
            Toast.makeText(context, "请先安装微信", Toast.LENGTH_SHORT).show();
            return;
        }

        WXMediaMessage msg = new WXMediaMessage();
        msg.setThumbImage(bitmap);

        com.tencent.mm.opensdk.modelmsg.WXImageObject imgObj = new com.tencent.mm.opensdk.modelmsg.WXImageObject(bitmap);
        msg.mediaObject = imgObj;

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("img");
        req.message = msg;
        req.scene = scene;

        api.sendReq(req);
    }

    private byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }
}
