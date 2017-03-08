package com.pampang.rnalipush;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.MessageReceiver;
import com.alibaba.sdk.android.push.common.util.JSONUtils;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.alibaba.sdk.android.push.notification.CPushMessage;
import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by PAMPANG on 2017/3/3.
 */

public class RNAlipush extends ReactContextBaseJavaModule {

    // static表示“全局”或者“静态”的意思，用来修饰成员变量和成员方法，也可以形成静态static代码块，但是Java语言中没有全局变量的概念。
    private static String TAG = "Alipush";
    private static RNAlipush sAlipush;
    private static ReactApplicationContext sRAC;
    private static Map<String, String> sStartUpPushMap = new HashMap<String, String>();

    // 构造方法
    public RNAlipush(ReactApplicationContext reactContext) {
        super(reactContext);
        sRAC = reactContext;
        sAlipush = this;
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

    /**
     * 检查是否存在未处理的startUpPush，有则将其返回
     * 目前这个方法可行，实测可以在app中拿到数据。不过仅限于noAction
     * @param promise: 用promise来做返回处理
     */
    @ReactMethod
    public void checkStartUpPush(Promise promise) {
        try {
            Log.i(TAG, "checkStartUpPush");
            WritableMap map = Arguments.createMap();
            // 如果sStartUpPushMap为空，则代表没有启动的推送
            if (sStartUpPushMap == null || sStartUpPushMap.size() < 1) {
                map.putBoolean("hasPush", false);
            } else {
                // 把sStartUpPushMap的数据全部返回
                Log.i(TAG, "startUpPushMap = " + sStartUpPushMap.toString());
                map.putBoolean("hasPush", true);
                map.putString("title", sStartUpPushMap.get("title"));
                map.putString("summary", sStartUpPushMap.get("summary"));
                map.putString("extraMap", sStartUpPushMap.get("extraMap"));
                sStartUpPushMap.clear();
            }
            promise.resolve(map);
        } catch (Exception e) {
            // 发生错误，则返回报错
            promise.reject(e.getMessage());
        }
    }

    public static class CustomMessageReceiver extends MessageReceiver {
        // 消息接收部分的LOG_TAG
        public static final String REC_TAG = "Alipush Receiver";
        public static final String ALIPUSH_ON_NOTIFICATION = "ALIPUSH_ON_NOTIFICATION";
        public static final String ALIPUSH_ON_NOTIFICATION_OPENED = "ALIPUSH_ON_NOTIFICATION_OPENED";
        public static final String ALIPUSH_ON_NOTIFICATION_OPENED_WITH_NO_ACTION = "ALIPUSH_ON_NOTIFICATION_OPENED_WITH_NO_ACTION";

        /**
         *
         * @param context: ReceiverRestrictedContext，此处的context不是我们希望的reactContext
         * @param title
         * @param summary
         * @param extraMap: 如："{a=11, _ALIYUN_NOTIFICATION_ID_=135459}"。 Map<String, String> 需要转换成 JSON 字符串
         * LOG: E/MyMessageReceiver: Receive notification, title: 奢 分 期, summary: 奢分期让你买买买不停！, extraMap: {a=11, _ALIYUN_NOTIFICATION_ID_=135459}
         */
        @Override
        public void onNotification(Context context, String title, String summary, Map<String, String> extraMap) {
            // TODO 处理推送通知
            // context --> ReceiverRestrictedContext
            // 此处的context不是我们希望的reactContext。看jpush-react-native的做法，是把当前的receiver写在RNAlipush这个类下，androidmanifest.xml写.RNAlipush$CutsomMessageReceiver，然后共享reactContext这个变量
            Log.e(REC_TAG, "Receive notification, title: " + title + ", summary: " + summary + ", extraMap: " + extraMap);
            // 退出后，处于保活状态，writableMap可用；而极光推送则是不可用的，估计react-native相关的组件已经被干掉了。
            // 然而，如果把deviceEventEmitter在appunmount之后取消订阅，则会无法接收到对应的消息

            // 收到消息，并非需要及时处理的。处理应该是在点击的时候进行。那么这里只需做消息的转达即可。用一个try-catch实现，catch里面不做东西（因为那时候react并没准备好）。
            try {
                // 创建传送到js端的数据组合
                WritableMap map = Arguments.createMap();
                map.putString("title", title);
                map.putString("summary", summary);
                map.putString("extraMap", convertMapToJson(extraMap));

                sRAC.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit(ALIPUSH_ON_NOTIFICATION, map);
            } catch (Throwable e) {
                Log.i(REC_TAG, "onNotification error");
                e.printStackTrace();
                // 目前构思，将当前推送信息存到module内，再分配一个主动获取的方法，在app启动完成后主动询问是否有未处理的推送
                sStartUpPushMap.put("title", title);
                sStartUpPushMap.put("summary", summary);
                sStartUpPushMap.put("extraMap", convertMapToJson(extraMap));
                Log.i(REC_TAG, "push data saved to sStartUpPushMap");
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
        public void onNotificationOpened(Context context, String title, String summary, String extraMap) {
            // 点击推送的时候，app已经在启动中了。所以此时判断app是否running无意义，因为一定running。
            Log.e(REC_TAG, "onNotificationOpened, title: " + title + ", summary: " + summary + ", extraMap:" + extraMap);

            // 收到消息，并非需要及时处理的。处理应该是在点击的时候进行。那么这里只需做消息的转达即可。用一个try-catch实现，catch里面不做东西（因为那时候react并没准备好）。
            try {
                // 创建传送到js端的数据组合
                WritableMap map = Arguments.createMap();

                map.putString("title", title);
                map.putString("summary", summary);
                map.putString("extraMap", extraMap);
                sRAC.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit(ALIPUSH_ON_NOTIFICATION_OPENED, map);
            } catch (Throwable e) {
                Log.i(REC_TAG, "onNotificationOpened error");
                e.printStackTrace();
                // 目前构思，将当前推送信息存到module内，再分配一个主动获取的方法，在app启动完成后主动询问是否有未处理的推送
                sStartUpPushMap.put("title", title);
                sStartUpPushMap.put("summary", summary);
                sStartUpPushMap.put("extraMap", extraMap);
                Log.i(REC_TAG, "push data saved to sStartUpPushMap");
            }
        }

        @Override
        protected void onNotificationClickedWithNoAction(Context context, String title, String summary, String extraMap) {
            /**
             * 在这里虽然isApplicationRunning会有对应的处理，但应用并不会自行启动。。。
             */
            Log.e(REC_TAG, "onNotificationClickedWithNoAction, title: " + title + ", summary: " + summary + ", extraMap:" + extraMap);
            // 收到消息，并非需要及时处理的。处理应该是在点击的时候进行。那么这里只需做消息的转达即可。用一个try-catch实现，catch里面不做东西（因为那时候react并没准备好）。
            try {
                Log.i(REC_TAG, "is App running: " + isApplicationRunning(context));
                if (!isApplicationRunning(context)) {
                    // 是否需要在这里加一个flag，来标识是否需要启动app?
                    startApplication(context);
                    throw new Exception("app is not running!");
                }
                // 创建传送到js端的数据组合
                WritableMap map = Arguments.createMap();
                map.putString("title", title);
                map.putString("summary", summary);
                map.putString("extraMap", extraMap);

                // 向Js端发送事件
                sRAC.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit(ALIPUSH_ON_NOTIFICATION_OPENED_WITH_NO_ACTION, map);
            } catch (Throwable e) {
                Log.i(REC_TAG, "onNotificationClickedWithNoAction error");
                e.printStackTrace();
                // 目前构思，将当前推送信息存到module内，再分配一个主动获取的方法，在app启动完成后主动询问是否有未处理的推送
                sStartUpPushMap.put("title", title);
                sStartUpPushMap.put("summary", summary);
                sStartUpPushMap.put("extraMap", extraMap);
                Log.i(REC_TAG, "push data saved to sStartUpPushMap");
            }
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
     * @param context 普通context即可，无须reactContext
     * @return
     */
    private static boolean isApplicationRunning(final Context context) {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        // getRunningTasks这里设置了maxNumber为100
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        for (ActivityManager.RunningTaskInfo info : list) {
            if (info.topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
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
     * 为noAction而弄的
     * @param context
     */
    private static void startApplication(Context context) {
        // 此方法仅用于打开app，无法跳转到对应的activity
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(sRAC.getPackageName());
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