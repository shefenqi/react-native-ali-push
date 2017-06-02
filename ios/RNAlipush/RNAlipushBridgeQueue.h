//
//  RNAlipushBridgeQueue.h
//  RNAlipush
//
//  Created by PAMPANG on 2017/3/9.
//  Copyright © 2017年 PAMPANG. All rights reserved.
//


#import <Foundation/Foundation.h>
#import "RNAlipush.h"

@interface RNAlipushBridgeQueue : NSObject

@property BOOL jsDidLoad;
@property NSDictionary* openedRemoteNotification;
@property NSDictionary* openedLocalNotification;

+ (nonnull instancetype)sharedInstance;

- (void)postNotification:(NSNotification *)notification status:(NSString *)status;
- (void)scheduleBridgeQueue;

@end
