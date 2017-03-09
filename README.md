# react-native-alipush

### 配置

```
npm i react-native-alipush -S
npm link react-native-alipush
```

### android配置

在Project根目录下`build.gradle`文件中配置maven库URL:

```
  allprojects {
      repositories {
          jcenter()
          maven {
              url 'http://maven.aliyun.com/nexus/content/repositories/releases/'
          }
      }
  }
```

在`MainApplication`的`onCreate`下，执行初始化alipush的方法：

```
  /**
  * 阿里云推送初始化
  */
  RNAlipush.initCloudChannel(this, ALIPUSH_APPKEY, ALIPUSH_APPSECRET);
```

### ios配置

podfile下加入：

```
source 'https://github.com/aliyun/aliyun-specs.git'

pod 'AlicloudPush', '~> 1.9.1'
```

### 解决utdid冲突的问题

错误如下：

```
  Error:Execution failed for task ':app:transformClassesWithJarMergingForDebug'.
  > com.android.build.api.transform.TransformException: java.util.zip.ZipException: duplicate entry: com/ta/utdid2/device/UTDevice.class
```

解决方法如下：
https://doc.open.alipay.com/doc2/detail.htm?treeId=54&articleId=104509&docType=1
