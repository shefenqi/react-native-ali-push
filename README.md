# react-native-ali-push

# 阿里云推送 for REACT-NATIVE

[![npm version](https://badge.fury.io/js/react-native-ali-push.svg)](https://badge.fury.io/js/react-native-ali-push)
### 配置

```
npm i react-native-ali-push -S
npm link react-native-ali-push
```

### android配置

在Project根目录下`build.gradle`文件中配置maven库URL:

```
  allprojects {
      repositories {
          jcenter()
          maven {
              url 'http://maven.aliyun.com/nexus/content/repositories/releases/'
          }
      }
  }
```

在`android/settings.gradle`中，加入：

```
  include ':react-native-ali-push'
  project(':react-native-ali-push').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-ali-push/android')
```

在`android/app/build.gradle`中，加入：

```
dependencies {
  ...
  ...
  compile project(':react-native-ali-push')      // <--- add this line
}
```

在`MainApplication`的`onCreate`下，执行初始化alipush的方法：

```
import com.pampang.rnalipush.RNAlipush;           // <--- add this line
import com.pampang.rnalipush.RNAlipushPackage;    // <--- add this line

......

  protected List<ReactPackage> getPackages() {
    return Arrays.<ReactPackage>asList(
      ......,
      new RNAlipushPackage()    // <--- add this line
    );
  };

  public void onCreate() {
    /**
    * 阿里云推送初始化
    */
    RNAlipush.initCloudChannel(this, ALIPUSH_APPKEY, ALIPUSH_APPSECRET);    // <--- add this line
  }
```

### ios配置

目前ios下仅支持用pod来安装`AlicloudPush`依赖.

podfile下加入：

```
source 'https://github.com/aliyun/aliyun-specs.git'

pod 'AlicloudPush', '~> 1.9.1'
```

在项目下加入公共包：

```
libz.tbd
libresolv.tbd
CoreTelephony.framework
SystemConfiguration.framework
libsqlite3.tbd（阿里云平台下载的SDK无需依赖，百川平台下载的SDK需要依赖）
```

在`AppDelegate.m`里加入（全部复制）：

```
// 阿里云推送
#import <CloudPushSDK/CloudPushSDK.h>
#import "RNAlipush.h"
#ifdef NSFoundationVersionNumber_iOS_9_x_Max
#import <UserNotifications/UserNotifications.h>
#endif

@interface AppDelegate () <UNUserNotificationCenterDelegate>
@end

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
  ......

  /**
   * react-native-ali-push
   */
  [self authorizeNotification];      // <--- add this line
  [RNAlipush initCloudPushWithAppKey:ALIPUSH_APP_KEY AndAppSecret:ALIPUSH_APP_SECRET];      // <--- add this line
  [RNAlipush application:application didFinishLaunchingWithOptions:launchOptions];      // <--- add this line

  ......
}

// ------------  react-native-ali-push start (Method settings)  --------------------
/**
 *  注册苹果APNs
 */
- (void)authorizeNotification {

  float systemVerson = [[UIDevice currentDevice].systemVersion floatValue];

  if (systemVerson >= 10.0) {
    // iOS 10 notifications
    UNUserNotificationCenter* center = [UNUserNotificationCenter currentNotificationCenter];
    center.delegate = self; // 遵循协议

    // 请求推送权限
    [center requestAuthorizationWithOptions:UNAuthorizationOptionAlert | UNAuthorizationOptionBadge | UNAuthorizationOptionSound completionHandler:^(BOOL granted, NSError* _Nullable error) {
      if (granted) {
        // granted
        NSLog(@"User authored notification.");
        // 向APNs注册，获取deviceToken 系统注册
        [[UIApplication sharedApplication] registerForRemoteNotifications];
      } else {
        // not granted
        NSLog(@"User denied notification.");
      }
    }];

    [center getNotificationSettingsWithCompletionHandler:^(UNNotificationSettings* _Nonnull settings) {
      // 进行判断做出相应的处理

      if (settings.authorizationStatus == UNAuthorizationStatusNotDetermined) {
        NSLog(@"未选择");
      } else if (settings.authorizationStatus == UNAuthorizationStatusDenied) {
        NSLog(@"未授权");
      } else if (settings.authorizationStatus == UNAuthorizationStatusAuthorized){
        NSLog(@"已授权");
      }
    }];

  } else if (systemVerson >= 8.0) { //适配 iOS_8, iOS_10.0

    // 提出弹窗，授权是否允许通知
    UIUserNotificationSettings* settings = [UIUserNotificationSettings settingsForTypes:UIUserNotificationTypeBadge|UIUserNotificationTypeSound|UIUserNotificationTypeAlert categories:nil];
    [[UIApplication sharedApplication] registerUserNotificationSettings:settings];

    // 注册远程通知 (iOS_8+)
    [[UIApplication sharedApplication] registerForRemoteNotifications];

    if ([[UIApplication sharedApplication] currentUserNotificationSettings].types  == UIUserNotificationTypeNone) { //判断用户是否打开通知开关
      NSLog(@"没有打开");
    } else {
      NSLog(@"已经打开");
    }

  } else { // 适配 iOS 8 之前的版本 3_0, 8_0
    UIRemoteNotificationType types = UIRemoteNotificationTypeBadge|UIRemoteNotificationTypeSound|UIRemoteNotificationTypeAlert;
    [[UIApplication sharedApplication] registerForRemoteNotificationTypes:types];

    if ([[UIApplication sharedApplication] enabledRemoteNotificationTypes]  == UIRemoteNotificationTypeNone) { //判断用户是否打开通知开关
    }
  }
}

/*
 *  苹果推送注册成功回调，将苹果返回的deviceToken上传到CloudPush服务器
 */
- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
  [RNAlipush application:application didRegisterForRemoteNotificationsWithDeviceToken:deviceToken];
}

/*
 *  苹果推送注册失败回调
 */
- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error {
  NSLog(@"didFailToRegisterForRemoteNotificationsWithError %@", error);
}

/*
 *  App处于启动状态时，通知打开回调
 */
- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo {
  [RNAlipush application:application didReceiveRemoteNotification:userInfo];
}

/**
 *  App正在前台 或者 用户点击通知栏的通知消息
 *  iOS 10以下 Remote Notification
 */
- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)   (UIBackgroundFetchResult))completionHandler {
  [RNAlipush application:application didReceiveRemoteNotification:userInfo fetchCompletionHandler:completionHandler];
}

/**
 *  App处于前台时收到通知(iOS 10+)
 */
- (void)userNotificationCenter:(UNUserNotificationCenter *)center willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler {
  [RNAlipush userNotificationCenter:center willPresentNotification:notification withCompletionHandler:completionHandler];
}

/**
 *  触发通知动作时回调，比如点击、删除通知和点击自定义action(iOS 10+)
 */
- (void)userNotificationCenter:(UNUserNotificationCenter *)center didReceiveNotificationResponse:(UNNotificationResponse *)response withCompletionHandler:(void (^)())completionHandler {
  [RNAlipush userNotificationCenter:center didReceiveNotificationResponse:response withCompletionHandler:completionHandler];
}
// ------------  react-native-ali-push end (Method settings)  --------------------
```

### js配置

在js下，仅需引入`react-native-ali-push`，并监听下述事件即可完成操作：

```
  /**
  * 添加listener，必须在app启动时执行的方法
  */
  RNAlipush.addOnNotificationReceivedListener((notification) => {
    console.log('on notification received', notification);
  });

  RNAlipush.addOnNotificationOpenedListener((notification) => {
    console.log('on notification opened', notification);
  });
```

### 解决utdid冲突的问题

错误如下：

```
  Error:Execution failed for task ':app:transformClassesWithJarMergingForDebug'.
  > com.android.build.api.transform.TransformException: java.util.zip.ZipException: duplicate entry: com/ta/utdid2/device/UTDevice.class
```

解决方法如下：
https://doc.open.alipay.com/doc2/detail.htm?treeId=54&articleId=104509&docType=1
