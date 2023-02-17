# Ip2region Java Implementation

![Java17](https://img.shields.io/badge/JDK-17+-success.svg)
[![GPLv3](https://img.shields.io/badge/License-GPLv3-blue.svg)](./LICENSE)
[![Author](https://img.shields.io/badge/Author-Harry_Yang-blue.svg)](https://github.com/TAKETODAY)

核心算法代码来自 [Ip2region](https://github.com/lionsoul2014/ip2region)


## 最佳实践

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

## 🙏 鸣谢

本项目的诞生离不开以下项目：
* [Ip2region](https://github.com/lionsoul2014/ip2region) 核心算法代码
* [Jetbrains](https://www.jetbrains.com/?from=https://github.com/TAKETODAY/ip2region-java) 感谢 Jetbrains 提供免费开源授权

## 📄 开源协议

使用 [GPLv3](https://github.com/TAKETODAY/ip2region-java/blob/master/LICENSE) 开源协议
