//
//  RNAlipushBridgeQueue.m
//  RNAlipush
//
//  Created by PAMPANG on 2017/3/9.
//  Copyright © 2017年 PAMPANG. All rights reserved.
//

#import "RNAlipushBridgeQueue.h"

@interface RNAlipushBridgeQueue () {
  NSMutableArray<NSDictionary *>* _bridgeQueue;
}

@end

@implementation RNAlipushBridgeQueue

+ (nonnull instancetype)sharedInstance {
  static RNAlipushBridgeQueue* sharedInstance = nil;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    sharedInstance = [self new];
  });
  
  return sharedInstance;
}


- (instancetype)init
{
  self = [super init];
  if (self) {
    self.jsDidLoad = NO;
    _bridgeQueue = [NSMutableArray new];
  }
  
  return self;
}

- (void)postNotification:(NSNotification *)notification {
  if (!_bridgeQueue) return;
    id obj = [notification object];
    [_bridgeQueue insertObject:obj atIndex:0];
}

- (void)scheduleBridgeQueue {
  for (NSDictionary *notification in _bridgeQueue) {
    [[NSNotificationCenter defaultCenter] postNotificationName:CCPDidReceiveApnsNotification object:notification];
  }
  [_bridgeQueue removeAllObjects];
}

@end
