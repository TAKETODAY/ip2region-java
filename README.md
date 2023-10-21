# Ip2region Java Implementation

![Java17](https://img.shields.io/badge/JDK-17+-success.svg)
[![GPLv3](https://img.shields.io/badge/License-GPLv3-blue.svg)](./LICENSE)
[![Author](https://img.shields.io/badge/Author-Harry_Yang-blue.svg)](https://github.com/TAKETODAY)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/22465e65998c41b38bcba4d8316da61a)](https://app.codacy.com/gh/TAKETODAY/ip2region-java?utm_source=github.com&utm_medium=referral&utm_content=TAKETODAY/ip2region-java&utm_campaign=Badge_Grade)

核心算法代码来自 [Ip2region](https://github.com/lionsoul2014/ip2region)

## 安装

```groovy
implementation 'cn.taketoday:ip2region-java:1.0-SNAPSHOT'
```

```xml
<dependency>
  <groupId>cn.taketoday</groupId>
  <artifactId>ip2region-java</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

## 最佳实践

### 静态工具类方式

```java
// 应用里有多处会用到搜索则可以使用一个静态工具类来调用
// 由于 ip2region.xdb 不算小，全局共享一个实例即可 

public abstract class IpUtils {

  private static final IpSearcher ipSearcher = IpSearcher.forDefaultResourceLocation();

  public static String search(String ip) {
    return ipSearcher.search(ip);
  }

  public static IpLocation find(String ip) {
    return ipSearcher.find(ip);
  }

}
```

### 依赖注入方式

```java
// 方式一

/**
 * 声明自己的API（也可以按需面向接口）到 IoC 容器，该方法可维护性更强
 */
@Component
public class IpSearcherOperations {

  static final IpSearcher ipSearcher = IpSearcher.forDefaultResourceLocation();
  // static final IpSearcher ipSearcher = IpSearcher.forResource(new ClassPathResource("ip2region.xdb"));

  public String search(String ip) {
    return ipSearcher.search(ip);
  }

  public IpLocation find(String ip) {
    return ipSearcher.find(ip);
  }

}

// 调用

/**
 * 调用端直接使用依赖注入
 */
@Component
public class Client {

  final IpSearcherOperations operations;

  Client(IpSearcherOperations operations) {
    this.operations = operations;
  }

  public void test() {
    String ip = "1.1.1.1";
    // String result = operations.search(ip);
    IpLocation ipLocation = operations.find(ip);
    if (ipLocation != null) {
      model.setIpCountry(ipLocation.getCountry());
      model.setIpProvince(ipLocation.getProvince());
      model.setIpCity(ipLocation.getCity());
      model.setIpArea(ipLocation.getArea());
      model.setIpIsp(ipLocation.getIsp());
    }
  }
}

// 方式二

/**
 * 直接声明 IpSearcher 组件，该方法代码更少可维护性不强
 */
@Configuration
public class AppConfig {

  @Bean
  static IpSearcher ipSearcher() {
    return IpSearcher.forDefaultResourceLocation();
  }

}

/**
 * 调用端直接使用依赖注入
 */
@Component
public class Client {

  final IpSearcher ipSearcher;

  Client(IpSearcher ipSearcher) {
    this.ipSearcher = ipSearcher;
  }

  public void test() {
    String ip = "1.1.1.1";
    // String result = ipSearcher.search(ip);
    IpLocation ipLocation = ipSearcher.find(ip);
    if (ipLocation != null) {
      model.setIpCountry(ipLocation.getCountry());
      model.setIpProvince(ipLocation.getProvince());
      model.setIpCity(ipLocation.getCity());
      model.setIpArea(ipLocation.getArea());
      model.setIpIsp(ipLocation.getIsp());
    }
  }
}

```

## 🙏 鸣谢

本项目的诞生离不开以下项目：

* [Ip2region](https://github.com/lionsoul2014/ip2region) 核心算法代码
* [Jetbrains](https://www.jetbrains.com/?from=https://github.com/TAKETODAY/ip2region-java) 感谢 Jetbrains 提供免费开源授权

## 📄 开源协议

使用 [GPLv3](https://github.com/TAKETODAY/ip2region-java/blob/master/LICENSE) 开源协议
