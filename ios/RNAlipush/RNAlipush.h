//
//  RNAlipush.h
//  RNAlipush
//
//  Created by PAMPANG on 2017/3/9.
//  Copyright © 2017年 PAMPANG. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
#import "RCTBridgeModule.h"
#import <CloudPushSDK/CloudPushSDK.h>
#import "RCTEventDispatcher.h"

@interface RNAlipush : NSObject <RCTBridgeModule>

- (void)initCloudPushWithAppKey:(NSString *)appKey AndAppSecret:(NSString *)appSecret;

@end
