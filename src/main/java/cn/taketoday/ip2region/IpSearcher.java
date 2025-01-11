/*
 * Original Author -> Harry Yang (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © Harry Yang & 2023 - 2025 All Rights Reserved.
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import infra.core.io.ClassPathResource;
import infra.core.io.FileSystemResource;
import infra.core.io.Resource;
import infra.lang.Assert;
import infra.lang.Nullable;

/**
 * 离线IP地址定位 API, 线程安全
 * <p>
 * 核心算法代码来自 <a href="https://github.com/lionsoul2014/ip2region">ip2region</a>
 * <p>
 * 例子
 * <pre> {@code
 *  static final IpSearcher ipSearcher = IpSearcher.forDefaultResourceLocation();
 *  static final IpSearcher ipSearcher = IpSearcher.forResource(
 *      new ClassPathResource("ip2region.xdb"));
 *
 *  IpLocation location = ipSearcher.find("8.8.8.8");
 * }
 * </pre>
 *
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @since 1.0 2023/2/14 17:26
 */
public abstract sealed class IpSearcher permits IpSearcher.MemoryIpSearcher {

  private static final String DEFAULT_LOCATION = "ip2region.xdb";

  // constant defined copied from the xdb maker
  private static final int HeaderInfoLength = 256;
  private static final int VectorIndexCols = 256;
  private static final int VectorIndexSize = 8;
  private static final int SegmentIndexSize = 14;

  private static final byte[] shiftIndex = { 24, 16, 8, 0 };

  /**
   * 返回结构化的 IP 地理数据
   *
   * @param ip 用户输入的 IP 地址
   * @return 结构化的地理数据
   */
  @Nullable
  public IpLocation find(String ip) {
    String search = search(ip);
    if (search != null) {
      String[] region = search.split("\\|");
      return new IpLocation(region[0], region[2], region[3], region[1], region[4]);
    }
    return null;
  }

  /**
   * 返回原始的的 IP 地理数据 使用 | 分割
   * <p>
   * 详细的格式参见 {@link #find(String)}
   *
   * @param ipString 用户输入的 IP 地址
   * @return 结构化的地理数据
   * @see #find(String)
   */
  @Nullable
  public String search(String ipString) {
    long ip = checkIP(ipString);
    return search(ip);
  }

  @Nullable
  public String search(long ip) {
    int il0 = (int) ((ip >> 24) & 0xFF);
    int il1 = (int) ((ip >> 16) & 0xFF);

    int idx = il0 * VectorIndexCols * VectorIndexSize + il1 * VectorIndexSize;

    final int indexPointerOffset = getIndexPointerOffset(idx);
    final byte[] indexPointerBuffer = getIndexPointerBuffer(idx);

    // locate the segment index block based on the vector index
    final int startIndexPtr = getInt(indexPointerBuffer, indexPointerOffset);
    final int endIndexPtr = getInt(indexPointerBuffer, indexPointerOffset + 4);

    // binary search the segment index block to get the region info
    final byte[] buff = new byte[SegmentIndexSize];
    int dataLen = -1;
    int dataPtr = -1;
    int l = 0;
    int h = (endIndexPtr - startIndexPtr) / SegmentIndexSize;
    while (l <= h) {
      int m = (l + h) >> 1;
      int p = startIndexPtr + m * SegmentIndexSize;

      // read the segment index
      read(p, buff);
      long sip = getIntLong(buff, 0);
      if (ip < sip) {
        h = m - 1;
      }
      else {
        long eip = getIntLong(buff, 4);
        if (ip > eip) {
          l = m + 1;
        }
        else {
          dataLen = getInt2(buff, 8);
          dataPtr = getInt(buff, 10);
          break;
        }
      }
    }

    // empty match interception
    // System.out.printf("dataLen: %d, dataPtr: %d\n", dataLen, dataPtr);
    if (dataPtr < 0) {
      return null;
    }

    // load and return the region data
    final byte[] regionBuff = new byte[dataLen];
    read(dataPtr, regionBuff);
    return new String(regionBuff, StandardCharsets.UTF_8);
  }

  protected abstract int getIndexPointerOffset(int idx);

  protected abstract byte[] getIndexPointerBuffer(int idx);

  protected abstract void read(int offset, byte[] buffer);

  /* get an int from a byte array start from the specified offset */
  static long getIntLong(byte[] b, int offset) {
    return (
            ((b[offset++] & 0x000000FFL)) |
                    ((b[offset++] << 8) & 0x0000FF00L) |
                    ((b[offset++] << 16) & 0x00FF0000L) |
                    ((b[offset] << 24) & 0xFF000000L)
    );
  }

  static int getInt(byte[] b, int offset) {
    return (((b[offset++] & 0x000000FF))
            | ((b[offset++] << 8) & 0x0000FF00)
            | ((b[offset++] << 16) & 0x00FF0000)
            | ((b[offset] << 24) & 0xFF000000));
  }

  static int getInt2(byte[] b, int offset) {
    return (
            ((b[offset++] & 0x000000FF)) |
                    ((b[offset] << 8) & 0x0000FF00)
    );
  }

  /**
   * check the specified ip address
   */
  static long checkIP(String ip) {
    String[] ps = ip.split("\\.");
    if (ps.length != 4) {
      throw new IllegalArgumentException("invalid ip address `%s`".formatted(ip));
    }

    long ipDst = 0;
    for (int i = 0; i < ps.length; i++) {
      int val = Integer.parseInt(ps[i]);
      if (val > 255) {
        throw new IllegalArgumentException("ip part `%s` should be less then 256".formatted(ps[i]));
      }

      ipDst |= ((long) val << shiftIndex[i]);
    }

    return ipDst & 0xFFFFFFFFL;
  }

  // Static Factory Methods

  /**
   * 使用默认的 {@link #DEFAULT_LOCATION classpath:ip2region.xdb}
   */
  public static IpSearcher forDefaultResourceLocation() {
    return forResource(new ClassPathResource(DEFAULT_LOCATION));
  }

  /**
   * Static factory methods for file path
   *
   * @param db ip2region.xdb file path
   * @return IpSearcher
   * @throws IllegalStateException cannot read db File to bytes
   */
  public static IpSearcher forFile(String db) {
    Assert.notNull(db, "db file is required");
    return forResource(new FileSystemResource(db));
  }

  /**
   * Static factory methods for File
   *
   * @param db ip2region.xdb file
   * @return IpSearcher
   * @throws IllegalStateException cannot read db File to bytes
   */
  public static IpSearcher forFile(File db) {
    Assert.notNull(db, "db file is required");
    return forResource(new FileSystemResource(db));
  }

  /**
   * Static factory methods for File
   *
   * @param contentBuffer ip2region.xdb file content buffer
   * @return IpSearcher
   */
  public static IpSearcher forBuffer(byte[] contentBuffer) {
    Assert.notNull(contentBuffer, "db ContentBuffer is required");
    return new MemoryIpSearcher(contentBuffer);
  }

  /**
   * Static factory methods for Resource
   *
   * @param resource ip2region.xdb resource, maybe a remote ip2region.xdb resource
   * @return IpSearcher
   * @throws IllegalStateException cannot read resource to bytes
   */
  public static IpSearcher forResource(Resource resource) {
    Assert.notNull(resource, "db resource is required");
    try (InputStream inputStream = resource.getInputStream()) {
      byte[] contentBuff = inputStream.readAllBytes();
      return forBuffer(contentBuff);
    }
    catch (IOException e) {
      throw new IllegalStateException("Cannot read ip2region db resource: " + resource);
    }
  }

  static final class MemoryIpSearcher extends IpSearcher {

    // xdb content buffer, used for in-memory search
    private final byte[] contentBuff;

    MemoryIpSearcher(byte[] contentBuff) {
      this.contentBuff = contentBuff;
    }

    @Override
    protected byte[] getIndexPointerBuffer(int idx) {
      return contentBuff;
    }

    @Override
    protected int getIndexPointerOffset(int idx) {
      return HeaderInfoLength + idx;
    }

    @Override
    protected void read(int offset, byte[] buffer) {
      System.arraycopy(contentBuff, offset, buffer, 0, buffer.length);
    }

  }

}
