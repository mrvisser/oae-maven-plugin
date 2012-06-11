/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.vtlgen.api;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 */
public class PackageUtil {

  public static final int BUFFER_MAX = 2048;
  
  public static void untar(String fileName, String targetPath) throws IOException {
    File tarArchiveFile = new File(fileName);
    BufferedOutputStream dest = null;
    FileInputStream tarArchiveStream = new FileInputStream(tarArchiveFile);
    TarArchiveInputStream tis = new TarArchiveInputStream(new BufferedInputStream(tarArchiveStream));
    TarArchiveEntry entry = null;
    try {
        while ((entry = tis.getNextTarEntry()) != null) {
            int count;
            File outputFile = new File(targetPath, entry.getName());

            if (entry.isDirectory()) { // entry is a directory
                if (!outputFile.exists()) {
                    outputFile.mkdirs();
                }
            } else { // entry is a file
                byte[] data = new byte[BUFFER_MAX];
                FileOutputStream fos = new FileOutputStream(outputFile);
                dest = new BufferedOutputStream(fos, BUFFER_MAX);
                while ((count = tis.read(data, 0, BUFFER_MAX)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
            }
        }
    } catch(Exception e) {
        e.printStackTrace();
    } finally {
        if (dest != null) {
            dest.flush();
            dest.close();
        }
        tis.close();
    }
  }
}
