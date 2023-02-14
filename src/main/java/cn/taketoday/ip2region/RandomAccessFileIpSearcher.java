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

/**
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @since 1.0 2023/2/14 22:10
 */
class RandomAccessFileIpSearcher extends IpSearcher {

  // random access file handle for file based search
  private final RandomAccessFile file;

  RandomAccessFileIpSearcher(RandomAccessFile file) {
    this.file = file;
  }

  @Override
  protected byte[] getIndexPointerBuffer(int idx) {
    final byte[] buff = new byte[VectorIndexSize];
    read(HeaderInfoLength + idx, buff);
    return buff;
  }

  @Override
  protected int getIndexPointerOffset(int idx) {
    return 0;
  }

  @Override
  protected void read(int offset, byte[] buffer) {
    // read from the file handle
    try {
      int rLen = file.read(buffer);
      if (rLen != buffer.length) {
        throw new IllegalStateException("incomplete read: read bytes should be " + buffer.length);
      }
    }
    catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public void close() throws IOException {
    if (this.file != null) {
      this.file.close();
    }
  }

}
