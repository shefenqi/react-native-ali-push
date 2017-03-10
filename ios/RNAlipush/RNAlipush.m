//
//  RNAlipush.m
//  RNAlipush
//
//  Created by PAMPANG on 2017/3/9.
//  Copyright © 2017年 PAMPANG. All rights reserved.
//

#import "RNAlipush.h"

@implementation RNAlipush

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(getDeviceId:(RCTPromiseResolveBlock)resolve
                  reject:(__unused RCTPromiseRejectBlock)reject) {
    resolve([CloudPushSDK getDeviceId]);
}

/**
 * 检查是否有未处理的push
 */
RCT_EXPORT_METHOD(checkStartUpPush:(RCTPromiseResolveBlock)resolve
                  reject:(__unused RCTPromiseRejectBlock)reject) {
    if (self.startUpNotification) {
        resolve(self.startUpNotification);
        self.startUpNotification = nil;
    } else {
        resolve(@"empty");
    }
}


/**
 * RCTEventEmitter的用法：http://blog.csdn.net/pz789as/article/details/52837853
 * 导出你所有的事件的名字，有多少写多少
 */
- (NSArray<NSString *> *)supportedEvents {
    return @[CCPDidReceiveMessageNotification, CCPDidReceiveApnsNotification, CCPDidOpenedApnsNotification];
}

/**
 * 会自行运行init。因为是react module。
 */
- (instancetype)init {
    self = [super init];
    self.didReactLoad = false;
    
    // 注册监听事件
    [self registerObserver];
    
    return self;
}

/**
 *  初始化alipush
 */
#pragma mark SDK Init
+ (void)initCloudPushWithAppKey:(NSString *)appKey AndAppSecret:(NSString *)appSecret {
    // 正式上线建议关闭
    [CloudPushSDK turnOnDebug];
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
 *    注册推送消息到来监听
 */
- (void)registerObserver {
    NSNotificationCenter *defaultCenter = [NSNotificationCenter defaultCenter];
    
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
    
    [defaultCenter addObserver:self
                      selector:@selector(reactDidload)
                          name:RCTJavaScriptDidLoadNotification
                        object:nil];

}


/**
 *    处理到来推送消息
 *
 *    @param     notification
 */
- (void)onMessageReceived:(NSNotification *)notification {
    if (!self.didReactLoad) {
        self.startUpNotification = notification;
        return;
    }
    
    CCPSysMessage *message = [notification object];
    NSString *title = [[NSString alloc] initWithData:message.title encoding:NSUTF8StringEncoding];
    NSString *body = [[NSString alloc] initWithData:message.body encoding:NSUTF8StringEncoding];
    NSLog(@"Receive message title: %@, content: %@.", title, body);
    
    // 在这里应该发送一个消息到js端
    [self sendEventWithName:CCPDidReceiveMessageNotification body:[notification userInfo]];
}


/**
 *    处理到来apns推送
 *
 *    @param     notification
 */
- (void)onApnsNotificationReceived:(NSNotification *)notification {
    id userInfo = [notification object];
    NSLog(@"userInfo if %@", userInfo);

    if (!self.didReactLoad) {
        self.startUpNotification = userInfo;
        return;
    }
    
    // 在这里应该发送一个消息到js端
    [self sendEventWithName:CCPDidReceiveApnsNotification body:userInfo];
}

- (void)reactDidload {
    NSLog(@"reactDidLoad");
    self.didReactLoad = true;
}

- (void)dealloc {
    NSLog(@"dealloc");
    // 避免exc_bac_access
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

@end
