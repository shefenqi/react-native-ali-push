//
//  RNAlipush.h
//  RNAlipush
//
//  Created by PAMPANG on 2017/3/9.
//  Copyright © 2017年 PAMPANG. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
#import <CloudPushSDK/CloudPushSDK.h>
#import <UserNotifications/UserNotifications.h>

#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#elif __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#elif __has_include("React/RCTBridgeModule.h")
#import "React/RCTBridgeModule.h"
#endif

// 收到自定义消息
#define CCPDidReceiveMessageNotification  @"CCPDidReceiveMessageNotification"

// 收到apn推送
#define CCPDidReceiveApnsNotification  @"CCPDidReceiveApnsNotification"
#define CCPDidOpenApnsNotification  @"CCPDidOpenApnsNotification"


@interface RNAlipush : NSObject <RCTBridgeModule>

@property (nonatomic) NSNotification *startUpNotification;

@property (nonatomic) BOOL didReactLoad;

+ (void)initCloudPushWithAppKey:(NSString *)appKey AndAppSecret:(NSString *)appSecret;
+ (void)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions;
+ (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken;
+ (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo;
+ (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)   (UIBackgroundFetchResult))completionHandler;
+ (void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification;
+ (void)userNotificationCenter:(UNUserNotificationCenter *)center willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler;
+ (void)userNotificationCenter:(UNUserNotificationCenter *)center didReceiveNotificationResponse:(UNNotificationResponse *)response withCompletionHandler:(void (^)())completionHandler;

@end
