//
//  RNAlipush.m
//  RNAlipush
//
//  Created by PAMPANG on 2017/3/9.
//  Copyright © 2017年 PAMPANG. All rights reserved.
//


#import "RNAlipush.h"
#import "RNAlipushBridgeQueue.h"

#if __has_include(<React/RCTBridge.h>)
#import <React/RCTEventDispatcher.h>
#import <React/RCTBridge.h>
#elif __has_include("RCTBridge.h")
#import "RCTEventDispatcher.h"
#import "RCTBridge.h"
#elif __has_include("React/RCTBridge.h")
#import "React/RCTEventDispatcher.h"
#import "React/RCTBridge.h"
#endif

@implementation RNAlipush

RCT_EXPORT_MODULE();
@synthesize bridge = _bridge;

// 单例模式
+ (id)allocWithZone:(NSZone *)zone {
    static RNAlipush *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [super allocWithZone:zone];
    });
    return sharedInstance;
}


+ (void)initCloudPushWithAppKey:(NSString *)appKey AndAppSecret:(NSString *)appSecret {
    // SDK初始化
    [CloudPushSDK asyncInit:appKey appSecret:appSecret callback:^(CloudPushCallbackResult *res) {
        if (res.success) {
            NSLog(@"Push SDK init success, deviceId: %@.", [CloudPushSDK getDeviceId]);
        } else {
            NSLog(@"Push SDK init failed, error: %@", res.error);
        }
    }];
}


/**
 *  当程序关闭时收到推送
 *  注册didFinishLaunchingWithOptions
 */
+ (void)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    NSLog(@"launchOptions: %@", launchOptions);
    // 注册苹果推送，获取deviceToken用于推送
    if ([[[UIDevice currentDevice] systemVersion] floatValue] >= 8.0) {
        // iOS 8 Notifications
        [application registerUserNotificationSettings:
         [UIUserNotificationSettings settingsForTypes:
          (UIUserNotificationTypeSound | UIUserNotificationTypeAlert | UIUserNotificationTypeBadge)
                                           categories:nil]];
        [application registerForRemoteNotifications];
    }
    
    // 点击通知将App从关闭状态启动时，将通知打开回执上报
    [CloudPushSDK sendNotificationAck:launchOptions];
}

+ (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    /**
     * alipush
     * 注册deviceToken
     */
    [CloudPushSDK registerDevice:deviceToken withCallback:^(CloudPushCallbackResult *res) {
        // 发送事件 CCPDidRegisterForRemoteNotificationsWithDeviceToken
        if (res.success) {
            NSLog(@"Register deviceToken success, deviceToken: %@", deviceToken);
        } else {
            NSLog(@"Register deviceToken failed, error: %@", res.error);
        }
    }];
}


+ (void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification {
    // 暂不处理
}


/**
 *  程序在打开时收到推送（ios 10 以下）
 *  called only when your app is running in the foreground
 */
+ (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo {
    
    NSLog(@"Receive one notification in didReceiveRemoteNotification.");
    NSLog(@"%@", userInfo);
    
    // iOS badge 处理
    if (application.applicationIconBadgeNumber > 0) {
        application.applicationIconBadgeNumber -= 1;
    } else {
        application.applicationIconBadgeNumber = 0;
    }

    // 判断程序是否在前台运行，是则加入app在前台的标识。
    // 其实需要的是：点击与否（ios10下，对应操作是应用在后台），收到与否（ios10下，对应操作是应用在前台）。
    if (application.applicationState == UIApplicationStateActive) {
        // 应用在前台，是收到了推送
        [[NSNotificationCenter defaultCenter] postNotificationName:CCPDidReceiveApnsNotification object:userInfo];
    } else {
        // 应用在后台，是打开了推送
        [[NSNotificationCenter defaultCenter] postNotificationName:CCPDidOpenApnsNotification object:userInfo];
    }

    // 通知打开回执上报
    [CloudPushSDK sendNotificationAck:userInfo];
}


/**
 *  程序在打开时收到推送（ios 10 以下）
 *  foreground or background
 */
+ (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {
    NSLog(@"Receive one notification in didReceiveRemoteNotification:completionHandler.");
    NSLog(@"%@", userInfo);
    
    // iOS badge 处理
    if (application.applicationIconBadgeNumber > 0) {
        application.applicationIconBadgeNumber -= 1;
    } else {
        application.applicationIconBadgeNumber = 0;
    }
    
    // 判断程序是否在前台运行，是则加入app在前台的标识。
    // 其实需要的是：点击与否（ios10下，对应操作是应用在后台），收到与否（ios10下，对应操作是应用在前台）。
    if (application.applicationState == UIApplicationStateActive) {
        // 应用在前台，是收到了推送
        [[NSNotificationCenter defaultCenter] postNotificationName:CCPDidReceiveApnsNotification object:userInfo];
    } else {
        // 应用在后台，是打开了推送
        [[NSNotificationCenter defaultCenter] postNotificationName:CCPDidOpenApnsNotification object:userInfo];
    }

    // 通知打开回执上报
    [CloudPushSDK sendNotificationAck:userInfo];
    
    // 执行默认操作
    completionHandler(UIBackgroundFetchResultNewData);
}



/**
 *  App处于前台时收到通知(iOS 10+)
 */
+ (void)userNotificationCenter:(UNUserNotificationCenter *)center willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler {
    
    NSLog(@"Receive a notification in foregound at alipush willPresentNotification.");
    
    // 处理iOS 10通知，并上报通知打开回执
    NSDictionary * userInfo = notification.request.content.userInfo;
    
    // 应用在前台，是收到了推送
    [[NSNotificationCenter defaultCenter] postNotificationName:CCPDidReceiveApnsNotification object:userInfo];

    // 通知打开回执上报
    [CloudPushSDK sendNotificationAck:userInfo];
    
    // 通知不弹出
//    completionHandler(UNNotificationPresentationOptionNone);
    
    // 通知弹出，且带有声音、内容和角标
    completionHandler(UNNotificationPresentationOptionSound | UNNotificationPresentationOptionAlert | UNNotificationPresentationOptionBadge);
    
}


/**
 *  触发通知动作时回调，比如点击、删除通知和点击自定义action(iOS 10+)
 */
+ (void)userNotificationCenter:(UNUserNotificationCenter *)center didReceiveNotificationResponse:(UNNotificationResponse *)response withCompletionHandler:(void (^)())completionHandler {
    
    NSLog(@"Receive a notification in foregound at alipush didReceiveNotificationResponse.");
    
//    int badge = [response.notification.request.content.badge intValue];
    
    // 处理iOS 10通知，并上报通知打开回执
    NSDictionary * userInfo = response.notification.request.content.userInfo;
    if([response.notification.request.trigger isKindOfClass:[UNPushNotificationTrigger class]]) {
        [[NSNotificationCenter defaultCenter] postNotificationName:CCPDidReceiveApnsNotification object:userInfo];
        // 通知打开回执上报
        [CloudPushSDK sendNotificationAck:userInfo];
    }
    completionHandler();
}


- (id)init {
    self = [super init];
    
    // 注册监听事件
    [self registerObserver];
    
    return self;
}

- (void)setBridge:(RCTBridge *)bridge {
    _bridge = bridge;
    
    // 实现APP在关闭状态通过点击推送打开时的推送处理
    [RNAlipushBridgeQueue sharedInstance].openedRemoteNotification = [_bridge.launchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey];
    [RNAlipushBridgeQueue sharedInstance].openedLocalNotification = [_bridge.launchOptions objectForKey:UIApplicationLaunchOptionsLocalNotificationKey];
}


/**
 *    注册推送消息到来监听
 */
- (void)registerObserver {
    NSNotificationCenter *defaultCenter = [NSNotificationCenter defaultCenter];
    
    [defaultCenter removeObserver:self];
    
    [defaultCenter addObserver:self
                      selector:@selector(jsDidLoad)
                          name:RCTJavaScriptDidLoadNotification
                        object:nil];
    
    // 自定义的消息。CCPDidReceiveMessageNotification是阿里云推送定义的。
    [defaultCenter addObserver:self
                      selector:@selector(onMessageReceived:)
                          name:CCPDidReceiveMessageNotification
                        object:nil];
    
    // 接收到ios官方apns推送。CCPDidReceiveApnsNotification是自定义的。
    [defaultCenter addObserver:self
                      selector:@selector(onApnsNotificationReceived:)
                          name:CCPDidReceiveApnsNotification
                        object:nil];
    
    // 打开ios官方apns推送。CCPDidOpenApnsNotification是自定义的。
    [defaultCenter addObserver:self
                      selector:@selector(onApnsNotificationOpened:)
                          name:CCPDidOpenApnsNotification
                        object:nil];
}



/**
 *    处理到来推送消息
 */
- (void)onMessageReceived:(NSNotification *)notification {
//    CCPSysMessage *message = [notification object];
//    NSString *title = [[NSString alloc] initWithData:message.title encoding:NSUTF8StringEncoding];
//    NSString *body = [[NSString alloc] initWithData:message.body encoding:NSUTF8StringEncoding];
//    NSLog(@"Receive message title: %@, content: %@.", title, body);
    
    [self.bridge.eventDispatcher sendAppEventWithName:CCPDidReceiveMessageNotification body:[notification userInfo]];
}


/**
 *    处理到来apns推送
 */
- (void)onApnsNotificationReceived:(NSNotification *)notification {
    id userInfo = [notification object];
    
    // 如果js部分未加载完，则先存档
    if ([RNAlipushBridgeQueue sharedInstance].jsDidLoad == YES) {
        [self.bridge.eventDispatcher sendAppEventWithName:CCPDidReceiveApnsNotification body:userInfo];
    } else {
        [[RNAlipushBridgeQueue sharedInstance] postNotification:notification status:@"receive"];
    }
}


- (void)onApnsNotificationOpened:(NSNotification *)notification {
    id userInfo = [notification object];
    
    // 如果js部分未加载完，则先存档
    if ([RNAlipushBridgeQueue sharedInstance].jsDidLoad == YES) {
        [self.bridge.eventDispatcher sendAppEventWithName:CCPDidOpenApnsNotification body:userInfo];
    } else {
        [[RNAlipushBridgeQueue sharedInstance] postNotification:notification status:@"open"];
    }
}


- (void)jsDidLoad {
    [RNAlipushBridgeQueue sharedInstance].jsDidLoad = YES;
    
    if ([RNAlipushBridgeQueue sharedInstance].openedRemoteNotification != nil) {
        [[NSNotificationCenter defaultCenter] postNotificationName:CCPDidReceiveApnsNotification object:[RNAlipushBridgeQueue sharedInstance].openedRemoteNotification];
    }
    
    if ([RNAlipushBridgeQueue sharedInstance].openedLocalNotification != nil) {
        [[NSNotificationCenter defaultCenter] postNotificationName:CCPDidReceiveApnsNotification object:[RNAlipushBridgeQueue sharedInstance].openedLocalNotification];
    }
    
    [[RNAlipushBridgeQueue sharedInstance] scheduleBridgeQueue];
}

- (void)dealloc {
    // 避免exc_bac_access
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

RCT_EXPORT_METHOD(turnOnDebug) {
    // 正式上线建议关闭
    [CloudPushSDK turnOnDebug];
}

RCT_EXPORT_METHOD(getDeviceId:(RCTPromiseResolveBlock)resolve
                  reject:(__unused RCTPromiseRejectBlock)reject) {
    resolve([CloudPushSDK getDeviceId]);
}

@end
