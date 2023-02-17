/*
 * Original Author -> Harry Yang (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © Harry Yang & 2023 All Rights Reserved.
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see [http://www.gnu.org/licenses/]
 */

package cn.taketoday.ip2region;

import java.util.Objects;

import cn.taketoday.core.style.ToStringBuilder;
import cn.taketoday.lang.Nullable;

/**
 * Ip 地理位置
 *
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @since 1.0 2023/2/14 17:57
 */
public class IpLocation {

  /** 本地网络 */
  public static final String LAN = "LAN";

  /** 未知地址 */
  public static final String UNKNOWN = "UNKNOWN";

  private final String country;
  private final String province;
  private final String city;
  private final String area;
  private final String isp;

  public IpLocation(String country, String province, String city, String area, String isp) {
    this.country = defaultValue(country);
    this.province = defaultValue(province);
    this.city = defaultValue(city);
    this.area = defaultValue(area);
    this.isp = defaultValue(isp);
  }

  static String defaultValue(@Nullable String area) {
    if (area == null || "0".equals(area)) {
      return UNKNOWN;
    }
    else if ("内网IP".equals(area)) {
      return LAN;
    }
    return area;
  }

  public String getCountry() {
    return country;
  }

  public String getProvince() {
    return province;
  }

  public String getCity() {
    return city;
  }

  public String getArea() {
    return area;
  }

  public String getIsp() {
    return isp;
  }

  @Override
  public String toString() {
    return ToStringBuilder.from(this)
            .append("country", country)
            .append("province", province)
            .append("city", city)
            .append("area", area)
            .append("isp", isp)
            .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof IpLocation that))
      return false;
    return Objects.equals(country, that.country)
            && Objects.equals(province, that.province)
            && Objects.equals(city, that.city)
            && Objects.equals(area, that.area)
            && Objects.equals(isp, that.isp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(country, province, city, area, isp);
  }

}
