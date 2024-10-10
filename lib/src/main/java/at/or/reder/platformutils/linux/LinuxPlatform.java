/*
 * Copyright 2024 Wolfgang Reder.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.or.reder.platformutils.linux;

import at.or.reder.platformutils.Distribution;
import at.or.reder.platformutils.Platform;
import at.or.reder.platformutils.util.LazyInitialized;
import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;
import lombok.Getter;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class LinuxPlatform implements Platform {
 
  private static final class InstanceHolder {

    private final static LinuxPlatform INSTANCE = new LinuxPlatform();
  }

  public static Platform getInstance()
  {
    return InstanceHolder.INSTANCE;
  }

  @Getter
  private final Lookup lookup;
  private final LazyInitialized<Distribution> distribution = new LazyInitialized<>(Distribution::detectDistribution);

  public LinuxPlatform()
  {
    LinuxPlatformFolders folders = new LinuxPlatformFolders();
    lookup = Lookups.fixed(folders,
                           new LinuxThumbnailService(folders),
                           new LinuxTrashService(folders),
                           new LinuxCommandService());
  }

  @Override
  public Distribution getDistribution()
  {
    return distribution.get();
  }

  private String execReadToString(String... execCommand) throws IOException
  {
    try (Scanner s = new Scanner(Runtime.getRuntime().exec(execCommand).getInputStream()).useDelimiter("\\A")) {
      return s.hasNext() ? s.next() : "";
    }
  }

  @Override
  public Optional<String> getHostName()
  {
    String result;
    result = System.getenv("HOSTNAME");
    if (result == null || result.isEmpty() || result.trim().isEmpty()) {
      try {
        result = execReadToString("hostname");
      } catch (IOException ex) {
        // ignore exception and try another solution
      }

    }
    if (result == null || result.isEmpty() || result.trim().isEmpty()) {
      try {
        result = execReadToString("cat", "/etc/hostname");
      } catch (IOException ex) {
        // ignore exception and try another solution
      }
    }
    if (result == null || result.isEmpty() || result.trim().isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(result);
    }
  }

}
