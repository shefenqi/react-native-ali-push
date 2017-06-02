import {
  DeviceEventEmitter,
  Platform,
  NativeModules,
} from 'react-native';

/**
 * 目前只支持android部分
 */
const RNAlipush = NativeModules.RNAlipush;

// android only
const ALIPUSH_ON_NOTIFICATION = 'ALIPUSH_ON_NOTIFICATION';
const ALIPUSH_ON_NOTIFICATION_OPENED = 'ALIPUSH_ON_NOTIFICATION_OPENED';
const ALIPUSH_ON_NOTIFICATION_OPENED_WITH_NO_ACTION = 'ALIPUSH_ON_NOTIFICATION_OPENED_WITH_NO_ACTION';

// ios only
const CCPDidReceiveApnsNotification = 'CCPDidReceiveApnsNotification';
const CCPDidOpenApnsNotification = 'CCPDidOpenApnsNotification';

export default class Alipush {

  /**
   * android && ios
   * 获取deviceId。直接返回一个promise
   */
  static getDeviceId() {
    return RNAlipush.getDeviceId();
  }

  /**
   * android ALIPUSH_ON_NOTIFICATION
   * ios CCPDidReceiveApnsNotification
   * @param {function} cb
   */
  static addOnNotificationReceivedListener(cb) {
    const EVENT_CODE = Platform.OS === 'android' ? ALIPUSH_ON_NOTIFICATION : CCPDidReceiveApnsNotification;

    DeviceEventEmitter.removeAllListeners(EVENT_CODE);
    DeviceEventEmitter.addListener(EVENT_CODE, (notification) => {
      if (cb) cb(notification);
    });
  }

  /**
   * android ALIPUSH_ON_NOTIFICATION_OPENED
   * ios CCPDidOpenApnsNotification
   * @param {function} cb
   */
  static addOnNotificationOpenedListener(cb) {
    const EVENT_CODE = Platform.OS === 'android' ? ALIPUSH_ON_NOTIFICATION_OPENED : CCPDidOpenApnsNotification;

    DeviceEventEmitter.removeAllListeners(EVENT_CODE);
    DeviceEventEmitter.addListener(EVENT_CODE, (notification) => {
      if (cb) cb(notification);
    });
  }

  /**
   * android only
   * @param {function} cb
   */
  static addOnNotificationOpenedWithNoActionListener(cb) {
    if (Platform.OS === 'ios') return;

    DeviceEventEmitter.removeAllListeners(ALIPUSH_ON_NOTIFICATION_OPENED_WITH_NO_ACTION);
    DeviceEventEmitter.addListener(ALIPUSH_ON_NOTIFICATION_OPENED_WITH_NO_ACTION, (notification) => {
      console.log(ALIPUSH_ON_NOTIFICATION_OPENED_WITH_NO_ACTION, notification);
      if (cb) cb(notification);
    });
  }
}