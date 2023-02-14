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
import cn.taketoday.util.StreamUtils;

/**
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @since 2.0 2023/2/14 17:26
 */
class IpSearcherTests {

  @Test
  void test() throws IOException {

    ClassPathResource resource = new ClassPathResource("ip2region.xdb");

    try (InputStream inputStream = resource.getInputStream()) {
      byte[] bytes = StreamUtils.copyToByteArray(inputStream);
      IpSearcher ipSearcher = IpSearcher.forBuffer(bytes);

      String search = ipSearcher.search("118.113.138.53");

      System.out.println(search);
    }
  }

  @Test
  void find() throws IOException {
    ClassPathResource resource = new ClassPathResource("ip2region.xdb");

    try (InputStream inputStream = resource.getInputStream()) {
      byte[] bytes = StreamUtils.copyToByteArray(inputStream);
      IpSearcher ipSearcher = IpSearcher.forBuffer(bytes);

      IpLocation search = ipSearcher.find("118.113.138.53");
      System.out.println(search);
    }
  }

}