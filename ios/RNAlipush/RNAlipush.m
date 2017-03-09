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


/**
 *  初始化alipush
 */
#pragma mark SDK Init
- (void)initCloudPushWithAppKey:(NSString *)appKey AndAppSecret:(NSString *)appSecret {
    // 如果已经初始化，则不再进行
    if (self) {
        return;
    }
    [super init];
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
    
    // 监听事件
    [self registerMessageReceive];
}

/**
 *    注册推送消息到来监听
 */
- (void)registerMessageReceive {
    NSNotificationCenter *defaultCenter = [NSNotificationCenter defaultCenter];
    [defaultCenter addObserver:self
                      selector:@selector(onMessageReceived:)
                          name:@"CCPDidReceiveMessageNotification"
                        object:nil];
     
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
    [self.bridge.eventDispatcher sendAppEventWithName:@"networkDidReceiveMessage"
                                                 body:[notification userInfo]];
}

@end
