/*
 * Original Author -> Harry Yang (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2023 All Rights Reserved.
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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import cn.taketoday.core.io.ClassPathResource;
import cn.taketoday.core.io.UrlResource;
import cn.taketoday.util.StreamUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @since 2.0 2023/2/14 17:26
 */
class IpSearcherTests {

  @Test
  void forBuffer() throws IOException {
    ClassPathResource resource = new ClassPathResource("ip2region.xdb");
    try (InputStream inputStream = resource.getInputStream()) {
      IpSearcher ipSearcher = IpSearcher.forBuffer(StreamUtils.copyToByteArray(inputStream));

      String location = ipSearcher.search("118.113.138.53");
      assertThat(location).isNotNull();
    }
  }

  @Test
  void local() {
    IpSearcher ipSearcher = IpSearcher.forResource(new ClassPathResource("ip2region.xdb"));
    var location = ipSearcher.find("127.0.0.1");
    assertThat(location).isNotNull();
    assertThat(IpLocation.defaultValue(location.getIsp())).isEqualTo(IpLocation.LAN);
    assertThat(IpLocation.defaultValue(location.getArea())).isEqualTo(IpLocation.UNKNOWN);
    assertThat(IpLocation.defaultValue(location.getCountry())).isEqualTo(IpLocation.UNKNOWN);
    assertThat(IpLocation.defaultValue(location.getProvince())).isEqualTo(IpLocation.UNKNOWN);
    assertThat(IpLocation.defaultValue(location.getCity())).isEqualTo(IpLocation.LAN);
  }

  @Test
  void find() {
    IpSearcher ipSearcher = IpSearcher.forResource(new ClassPathResource("ip2region.xdb"));
    IpLocation location = ipSearcher.find("118.113.138.53");
    assertThat(location).isNotNull();
  }

  @Test
  void defaultValue() {
    assertThat(IpLocation.defaultValue(null)).isEqualTo(IpLocation.UNKNOWN);
    assertThat(IpLocation.defaultValue("0")).isEqualTo(IpLocation.UNKNOWN);
    assertThat(IpLocation.defaultValue("内网IP")).isEqualTo(IpLocation.LAN);
  }

  @Test
  void forResource() {
    IpSearcher ipSearcher = IpSearcher.forResource(new ClassPathResource("ip2region.xdb"));
    IpLocation ipLocation = ipSearcher.find("118.113.138.53");
    assertThat(ipLocation).isNotNull();
  }

  @Test
  void forDefaultResourceLocation() {
    IpSearcher ipSearcher = IpSearcher.forDefaultResourceLocation();
    IpLocation ipLocation = ipSearcher.find("118.113.138.53");
    assertThat(ipLocation).isNotNull();
  }

  @Test
  void forHttpResource() {
    IpSearcher ipSearcher = IpSearcher.forResource(UrlResource.from("https://raw.githubusercontent.com/lionsoul2014/ip2region/master/data/ip2region.xdb"));
    IpLocation ipLocation = ipSearcher.find("118.113.138.53");
    assertThat(ipLocation).isNotNull();
  }

}