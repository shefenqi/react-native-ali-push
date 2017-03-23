package com.pampang.rnalipush;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.MessageReceiver;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.alibaba.sdk.android.push.notification.CPushMessage;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.facebook.react.common.ApplicationHolder.getApplication;

/**
 * Created by PAMPANG on 2017/3/3.
 */

public class RNAlipush extends ReactContextBaseJavaModule {

    private static String TAG = "Alipush";
    private static RNAlipush sAlipush;
    private static ReactApplicationContext sReactContext;
    private static Map<String, String> sStartUpPushMap = new HashMap<String, String>();

    public RNAlipush(ReactApplicationContext reactContext) {
        super(reactContext);
        sReactContext = reactContext;
        sAlipush = this;
    }

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
                Log.d(TAG, "init cloudchannel success! response: " + response);
                Log.d(TAG, "device Id: " + pushService.getDeviceId());

            }
            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.d(TAG, "init cloudchannel failed -- errorcode:" + errorCode + " -- errorMessage:" + errorMessage);
            }
        });
    }

    /**
     * 获取DeviceId
     * @param promise: 直接通过promise返回获取deviceId
     */
    @ReactMethod
    public void getDeviceId(Promise promise) {
        final CloudPushService pushService = PushServiceFactory.getCloudPushService();
        promise.resolve(pushService.getDeviceId());
    }

    public static class CustomMessageReceiver extends MessageReceiver {
        public static final String REC_TAG = "Alipush Receiver";
        public static final String ALIPUSH_ON_NOTIFICATION = "ALIPUSH_ON_NOTIFICATION";
        public static final String ALIPUSH_ON_NOTIFICATION_OPENED = "ALIPUSH_ON_NOTIFICATION_OPENED";
        public static final String ALIPUSH_ON_NOTIFICATION_OPENED_WITH_NO_ACTION = "ALIPUSH_ON_NOTIFICATION_OPENED_WITH_NO_ACTION";

        /**
         * 推送处理入口
         * @param eventName
         * @param title
         * @param summary
         * @param extraMap
         */
        private void handleNotification(final String eventName, final String title, final String summary, final String extraMap) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    // Construct and load our normal React JS code bundle
                    ReactInstanceManager mReactInstanceManager = ((ReactApplication) getApplication()).getReactNativeHost().getReactInstanceManager();
                    ReactContext context = mReactInstanceManager.getCurrentReactContext();
                    // If it's constructed, send a notification
                    if (context != null) {
                        sendEvent(eventName, convertToWritableMap(title, summary, extraMap));
                    } else {
                        // Otherwise wait for construction, then send the notification
                        mReactInstanceManager.addReactInstanceEventListener(new ReactInstanceManager.ReactInstanceEventListener() {
                            public void onReactContextInitialized(ReactContext context) {
                                sendEvent(eventName, convertToWritableMap(title, summary, extraMap));
                            }
                        });
                        if (!mReactInstanceManager.hasStartedCreatingInitialContext()) {
                            // Construct it in the background
                            mReactInstanceManager.createReactContextInBackground();
                        }
                    }
                }
            });
        }

        /**
         * 发送事件到js端
         * @param eventName
         * @param map
         */
        private void sendEvent(String eventName, @Nullable WritableMap map) {
            // 此处需要添加hasActiveCatalystInstance，否则可能造成崩溃
            // 问题解决参考: https://github.com/walmartreact/react-native-orientation-listener/issues/8
            if(sReactContext.hasActiveCatalystInstance()) {
                Log.i(REC_TAG, "hasActiveCatalystInstance");
                sReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit(eventName, map);
            } else {
                Log.i(REC_TAG, "not hasActiveCatalystInstance");
            }
        }

        /**
         * 转换推送数据为writableMap
         * @param title
         * @param summary
         * @param extraMap
         * @return
         */
        private WritableMap convertToWritableMap(String title, String summary, String extraMap) {
            WritableMap map = Arguments.createMap();
            map.putString("title", title);
            map.putString("summary", summary);
            map.putString("extraMap", extraMap);
            return map;
        }

        /**
         *
         * @param context
         * @param title
         * @param summary
         * @param extraMap: 如："{a=11, _ALIYUN_NOTIFICATION_ID_=135459}"。 Map<String, String> 需要转换成 JSON 字符串
         * LOG: E/MyMessageReceiver: Receive notification, title: 奢 分 期, summary: 奢分期让你买买买不停！, extraMap: {a=11, _ALIYUN_NOTIFICATION_ID_=135459}
         */
        @Override
        public void onNotification(Context context, String title, String summary, Map<String, String> extraMap) {
            Log.e(REC_TAG, "Receive notification, title: " + title + ", summary: " + summary + ", extraMap: " + extraMap);
            if(sReactContext != null && sReactContext.getCurrentActivity() != null) {
                Log.e(REC_TAG, "app is ready, sending event.");
                sendEvent(ALIPUSH_ON_NOTIFICATION, convertToWritableMap(title, summary, convertMapToJson(extraMap)));
            }
        }

        /**
         * 需明白alipush里面message的定义
         * @param context
         * @param cPushMessage
         */
        @Override
        public void onMessage(Context context, CPushMessage cPushMessage) {
            Log.e(REC_TAG, "onMessage, messageId: " + cPushMessage.getMessageId() + ", title: " + cPushMessage.getTitle() + ", content:" + cPushMessage.getContent());
        }

        /**
         * 通知被打开
         * @param context
         * @param title
         * @param summary
         * @param extraMap: 此时的extraMap为JSON格式的String
         */
        @Override
        public void onNotificationOpened(Context context, final String title, final String summary, final String extraMap) {
            Log.e(REC_TAG, "onNotificationOpened, title: " + title + ", summary: " + summary + ", extraMap:" + extraMap);
            handleNotification(ALIPUSH_ON_NOTIFICATION_OPENED, title, summary, extraMap);
        }

        @Override
        protected void onNotificationClickedWithNoAction(Context context, String title, String summary, String extraMap) {
            Log.e(REC_TAG, "onNotificationClickedWithNoAction, title: " + title + ", summary: " + summary + ", extraMap:" + extraMap);
            handleNotification(ALIPUSH_ON_NOTIFICATION_OPENED_WITH_NO_ACTION, title, summary, extraMap);
        }

        @Override
        protected void onNotificationReceivedInApp(Context context, String title, String summary, Map<String, String> extraMap, int openType, String openActivity, String openUrl) {
            Log.e(REC_TAG, "onNotificationReceivedInApp, title: " + title + ", summary: " + summary + ", extraMap:" + extraMap + ", openType:" + openType + ", openActivity:" + openActivity + ", openUrl:" + openUrl);
        }

        @Override
        protected void onNotificationRemoved(Context context, String messageId) {
            Log.e(REC_TAG, "onNotificationRemoved");
        }
    }

    /**
     * 查看application是否正在运行
     * @param context
     * @return
     */
    private static boolean isApplicationRunning(final Context context) {
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        if (processInfos != null) {
            for (ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
                if (processInfo.processName.equals(getApplication().getPackageName())) {
                    if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        for (String d : processInfo.pkgList) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 查看application是否正在后台运行
     * @param context 普通context即可，无须reactContext
     * @return
     */
    private static boolean isApplicationRunningBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 打开app
     * @param context
     */
    private static void startApplication(Context context) {
        // 此方法仅用于打开app，无法跳转到对应的activity
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(sReactContext.getPackageName());
        context.startActivity(launchIntent);
    }

    /**
     * 转换map为json格式的字符串
     * @param map
     * @return json格式的字符串
     */
    private static String convertMapToJson(Map map) {
        return new JSONObject(map).toString();
    }
}