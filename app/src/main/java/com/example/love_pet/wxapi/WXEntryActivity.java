package com.example.love_pet.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.love_pet.R;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final String APP_ID = "wx1234567890abcdef";
    private IWXAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
    }

    @Override
    public void onResp(BaseResp resp) {
        if (resp.getType() == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
            String result;
            switch (resp.errCode) {
                case BaseResp.ErrCode.ERR_OK:
                    result = "分享成功";
                    break;
                case BaseResp.ErrCode.ERR_USER_CANCEL:
                    result = "分享取消";
                    break;
                case BaseResp.ErrCode.ERR_AUTH_DENIED:
                    result = "分享被拒绝";
                    break;
                default:
                    result = "分享失败";
                    break;
            }
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
