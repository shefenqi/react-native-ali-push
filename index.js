import {
  DeviceEventEmitter,
  NativeEventEmitter,
  Platform,
  NativeModules,
} from 'react-native';
const { RNAlipush } = NativeModules;

// 根据不同的平台，选择不同的eventEmitter
const EventEmitter = Platform.OS === 'android' ? DeviceEventEmitter : new NativeEventEmitter(RNAlipush);

// android only
const ALIPUSH_ON_NOTIFICATION = 'ALIPUSH_ON_NOTIFICATION';
const ALIPUSH_ON_NOTIFICATION_OPENED = 'ALIPUSH_ON_NOTIFICATION_OPENED';
const ALIPUSH_ON_NOTIFICATION_OPENED_WITH_NO_ACTION = 'ALIPUSH_ON_NOTIFICATION_OPENED_WITH_NO_ACTION';

// ios only
const ALIPUSH_DID_RECEIVE_APNS_NOTIFICATION = 'CCPDidReceiveApnsNotification';

export default class Alipush {

  /**
   * android && ios
   * 获取deviceId。直接返回一个promise
   */
  static getDeviceId() {
    return RNAlipush.getDeviceId();
  }

  /**
   * android only
   * 检查是否存在未处理的native推送
   */
  static checkStartUpPush() {
    return RNAlipush.checkStartUpPush();
  }

  /**
   * android only
   * @param {function} cb
   */
  static addOnNotificationListener(cb) {
    if (Platform.OS === 'ios') return;
    // 去掉原有的事件。在这里去掉而不在app unmount时去掉，是因为unmount时需要用到。
    EventEmitter.removeAllListeners(ALIPUSH_ON_NOTIFICATION);

    EventEmitter.addListener(ALIPUSH_ON_NOTIFICATION, (notification) => {
      console.log(ALIPUSH_ON_NOTIFICATION, notification);
      if (cb) cb(notification);
    });
  }

  /**
   * android only
   * @param {function} cb
   */
  static addOnNotificationOpenedListener(cb) {
    if (Platform.OS === 'ios') return;
    // 去掉原有的事件。在这里去掉而不在app unmount时去掉，是因为unmount时需要用到。
    EventEmitter.removeAllListeners(ALIPUSH_ON_NOTIFICATION_OPENED);
    EventEmitter.addListener(ALIPUSH_ON_NOTIFICATION_OPENED, (notification) => {
      console.log(ALIPUSH_ON_NOTIFICATION_OPENED, notification);
      if (cb) cb(notification);
    });
  }

  /**
   * android only
   * @param {function} cb
   */
  static addOnNotificationOpenedWithNoActionListener(cb) {
    if (Platform.OS === 'ios') return;
    // 去掉原有的事件。在这里去掉而不在app unmount时去掉，是因为unmount时需要用到。
    EventEmitter.removeAllListeners(ALIPUSH_ON_NOTIFICATION_OPENED_WITH_NO_ACTION);
    EventEmitter.addListener(ALIPUSH_ON_NOTIFICATION_OPENED_WITH_NO_ACTION, (notification) => {
      console.log(ALIPUSH_ON_NOTIFICATION_OPENED_WITH_NO_ACTION, notification);
      if (cb) cb(notification);
    });
  }

  /**
   * ios only
   * @param {function} cb
   */
  static addDidReceiveApnsNotificationListener(cb) {
    if (Platform.OS === 'android') return;
    // 去掉原有的事件。在这里去掉而不在app unmount时去掉，是因为unmount时需要用到。
    EventEmitter.removeAllListeners(ALIPUSH_DID_RECEIVE_APNS_NOTIFICATION);
    EventEmitter.addListener(ALIPUSH_DID_RECEIVE_APNS_NOTIFICATION, (notification) => {
      console.log(ALIPUSH_DID_RECEIVE_APNS_NOTIFICATION, notification);
      if (cb) cb(notification);
    });
  }
}