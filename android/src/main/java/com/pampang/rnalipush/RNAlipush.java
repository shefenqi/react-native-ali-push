package com.pampang.rnalipush;
import android.content.Context;
import android.util.Log;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

/**
 * Created by PAMPANG on 2017/3/3.
 */

public class RNAlipush extends ReactContextBaseJavaModule {

    // 构造方法
    public RNAlipush(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    // 覆写getName方法，它返回一个字符串名字，在JS中我们就使用这个名字调用这个模块
    @Override
    public String getName() {
        return "RNAlipush";
    }

    /**
     * 初始化云推送通道
     * @param applicationContext
     */
    public static void initCloudChannel(Context applicationContext, String appKey, String appSecret) {
        PushServiceFactory.init(applicationContext);
        final CloudPushService pushService = PushServiceFactory.getCloudPushService();
        pushService.register(applicationContext, appKey, appSecret, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d("alipush", "init cloudchannel success! response: " + response);
                Log.d("alipush", "device Id: " + pushService.getDeviceId());

            }
            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.d("alipush", "init cloudchannel failed -- errorcode:" + errorCode + " -- errorMessage:" + errorMessage);
            }
        });
    }
}