import {
  DeviceEventEmitter,
  NativeAppEventEmitter,
  Platform,
  NativeModules,
} from 'react-native';
const { RNAlipush } = NativeModules;

// 根据不同的平台，选择不同的eventEmitter
const EventEmitter = Platform.OS === 'android' ? DeviceEventEmitter : NativeAppEventEmitter;
const ALIPUSH_ON_NOTIFICATION = 'ALIPUSH_ON_NOTIFICATION';
const ALIPUSH_ON_NOTIFICATION_OPENED = 'ALIPUSH_ON_NOTIFICATION_OPENED';
const ALIPUSH_ON_NOTIFICATION_OPENED_WITH_NO_ACTION = 'ALIPUSH_ON_NOTIFICATION_OPENED_WITH_NO_ACTION';

export default class Alipush {

  /**
   * 获取deviceId。直接返回一个promise
   *
   * @static
   * @returns promise
   *
   * @memberOf RNAlipush
   */
  static getDeviceId() {
    return RNAlipush.getDeviceId();
  }

  static checkStartUpPush() {
    return RNAlipush.checkStartUpPush();
  }

  static addOnNotificationListener(cb) {
    // 去掉原有的事件。在这里去掉而不在app unmount时去掉，是因为unmount时需要用到。
    EventEmitter.removeAllListeners(ALIPUSH_ON_NOTIFICATION);

    EventEmitter.addListener(ALIPUSH_ON_NOTIFICATION, (notification) => {
      console.log(ALIPUSH_ON_NOTIFICATION, notification);
      if (cb) cb(notification);
    });
  }

  static addOnNotificationOpenedListener(cb) {
    // 去掉原有的事件。在这里去掉而不在app unmount时去掉，是因为unmount时需要用到。
    EventEmitter.removeAllListeners(ALIPUSH_ON_NOTIFICATION_OPENED);
    EventEmitter.addListener(ALIPUSH_ON_NOTIFICATION_OPENED, (notification) => {
      console.log(ALIPUSH_ON_NOTIFICATION_OPENED, notification);
      if (cb) cb(notification);
    });
  }

  static addOnNotificationOpenedWithNoActionListener(cb) {
    // 去掉原有的事件。在这里去掉而不在app unmount时去掉，是因为unmount时需要用到。
    EventEmitter.removeAllListeners(ALIPUSH_ON_NOTIFICATION_OPENED_WITH_NO_ACTION);
    EventEmitter.addListener(ALIPUSH_ON_NOTIFICATION_OPENED_WITH_NO_ACTION, (notification) => {
      console.log(ALIPUSH_ON_NOTIFICATION_OPENED_WITH_NO_ACTION, notification);
      if (cb) cb(notification);
    });
  }
}