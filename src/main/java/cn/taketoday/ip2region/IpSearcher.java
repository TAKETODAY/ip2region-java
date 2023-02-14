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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

/**
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @since 1.0 2023/2/14 17:26
 */
public abstract class IpSearcher {

  // constant defined copied from the xdb maker
  public static final int HeaderInfoLength = 256;
  public static final int VectorIndexRows = 256;
  public static final int VectorIndexCols = 256;
  public static final int VectorIndexSize = 8;
  public static final int SegmentIndexSize = 14;

  public static final byte[] shiftIndex = { 24, 16, 8, 0 };

  public IpLocation find(String ip) {
    String search = search(ip);
    if (search != null) {
      String[] region = search.split("\\|");
      return new IpLocation(region[0], region[2], region[3], region[1], region[4]);
    }
    return null;
  }

  public String search(String ipStr) {
    long ip = checkIP(ipStr);
    return search(ip);
  }

  public String search(long ip) {
    int il0 = (int) ((ip >> 24) & 0xFF);
    int il1 = (int) ((ip >> 16) & 0xFF);

    int idx = il0 * VectorIndexCols * VectorIndexSize + il1 * VectorIndexSize;

    // System.out.printf("il0: %d, il1: %d, idx: %d\n", il0, il1, idx);

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

  // --- static cache util function

  public static Header loadHeader(RandomAccessFile handle) throws IOException {
    handle.seek(0);
    final byte[] buff = new byte[HeaderInfoLength];
    handle.read(buff);
    return new Header(buff);
  }

  public static Header loadHeaderFromFile(String dbPath) throws IOException {
    final RandomAccessFile handle = new RandomAccessFile(dbPath, "r");
    final Header header = loadHeader(handle);
    handle.close();
    return header;
  }

  public static byte[] loadVectorIndex(RandomAccessFile handle) throws IOException {
    handle.seek(HeaderInfoLength);
    int len = VectorIndexRows * VectorIndexCols * VectorIndexSize;
    final byte[] buff = new byte[len];
    int rLen = handle.read(buff);
    if (rLen != len) {
      throw new IOException("incomplete read: read bytes should be " + len);
    }

    return buff;
  }

  public static byte[] loadVectorIndexFromFile(String dbPath) throws IOException {
    final RandomAccessFile handle = new RandomAccessFile(dbPath, "r");
    final byte[] vIndex = loadVectorIndex(handle);
    handle.close();
    return vIndex;
  }

  public static byte[] loadContent(RandomAccessFile handle) throws IOException {
    handle.seek(0);
    final byte[] buff = new byte[(int) handle.length()];
    int rLen = handle.read(buff);
    if (rLen != buff.length) {
      throw new IOException("incomplete read: read bytes should be " + buff.length);
    }

    return buff;
  }

  public static byte[] loadContentFromFile(String dbPath) throws IOException {
    final RandomAccessFile handle = new RandomAccessFile(dbPath, "r");
    final byte[] content = loadContent(handle);
    handle.close();
    return content;
  }

  // --- End cache load util function

  // --- static util method

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

  /* long int to ip string */
  public static String long2ip(long ip) {
    return String.valueOf((ip >> 24) & 0xFF) + '.' +
            ((ip >> 16) & 0xFF) + '.' + ((ip >> 8) & 0xFF) + '.' + ((ip) & 0xFF);
  }

  /* check the specified ip address */
  static long checkIP(String ip) {
    String[] ps = ip.split("\\.");
    if (ps.length != 4) {
      throw new IllegalArgumentException("invalid ip address `" + ip + "`");
    }

    long ipDst = 0;
    for (int i = 0; i < ps.length; i++) {
      int val = Integer.parseInt(ps[i]);
      if (val > 255) {
        throw new IllegalArgumentException("ip part `" + ps[i] + "` should be less then 256");
      }

      ipDst |= ((long) val << shiftIndex[i]);
    }

    return ipDst & 0xFFFFFFFFL;
  }

  // Static Factory Methods

  public static IpSearcher forVectorIndex(byte[] cBuff) {
    return new VectorIndexIpSearcher(cBuff);
  }

  public static IpSearcher forFile(String db) throws IOException {
    return new RandomAccessFileIpSearcher(new RandomAccessFile(db, "r"));
  }

  public static IpSearcher forBuffer(byte[] cBuff) {
    return new MemIpSearcher(cBuff);
  }

  public static class Header {

    public final int version;
    public final int indexPolicy;
    public final int createdAt;
    public final int startIndexPtr;
    public final int endIndexPtr;
    public final byte[] buffer;

    public Header(byte[] buff) {
      version = getInt2(buff, 0);
      indexPolicy = getInt2(buff, 2);
      createdAt = getInt(buff, 4);
      startIndexPtr = getInt(buff, 8);
      endIndexPtr = getInt(buff, 12);
      buffer = buff;
    }

  }

}
