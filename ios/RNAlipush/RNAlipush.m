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

RCT_REMAP_METHOD(getDeviceId,
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    resolve([CloudPushSDK getDeviceId]);
}

/**
 * 会自行运行init。因为是react module。
 */
- (instancetype)init {
    self = [super init];
    
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
    
//    // 打开ios官方apns推送。CCPDidOpenApnsNotification是自定义的。
//    [defaultCenter addObserver:self
//                      selector:@selector(onApnsNotificationOpened:)
//                          name:CCPDidOpenedApnsNotification
//                        object:nil];
    
//    [defaultCenter addObserver:self
//                      selector:@selector(reactJSDidload)
//                          name:RCTJavaScriptDidLoadNotification
//                        object:nil];

}


/**
 *    处理到来推送消息
 *
 *    @param     notification
 */
- (void)onMessageReceived:(NSNotification *)notification {
    CCPSysMessage *message = [notification object];
    NSString *title = [[NSString alloc] initWithData:message.title encoding:NSUTF8StringEncoding];
    NSString *body = [[NSString alloc] initWithData:message.body encoding:NSUTF8StringEncoding];
    NSLog(@"Receive message title: %@, content: %@.", title, body);
    
    // 在这里应该发送一个消息到js端
    [self.bridge.eventDispatcher sendAppEventWithName:CCPDidReceiveMessageNotification
                                                 body:[notification userInfo]];
}


/**
 *    处理到来apns推送
 *
 *    @param     notification
 */
- (void)onApnsNotificationReceived:(NSNotification *)notification {
    CCPSysMessage *message = [notification object];
    NSString *title = [[NSString alloc] initWithData:message.title encoding:NSUTF8StringEncoding];
    NSString *body = [[NSString alloc] initWithData:message.body encoding:NSUTF8StringEncoding];
    NSLog(@"Receive message title: %@, content: %@.", title, body);
    
    // 在这里应该发送一个消息到js端
    [self.bridge.eventDispatcher sendAppEventWithName:CCPDidReceiveApnsNotification
                                                 body:[notification userInfo]];
}

@end
