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
package at.or.reder.platformutils;

import java.io.BufferedInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@Messages({"Distribution_WINDOWS_MS=Windows",
  "Distribution_LINUX_REDHAT=Linux/RedHat",
  "Distribution_LINUX_SUSE=Linux/OpenSuse",
  "Distribution_LINUX_RPI=Linux/Raspberry PI",
  "Distribution_LINUX_DEBIAN=Linux/Debian",
  "Distribution_LINUX_UBUNTU=Linux/Ubuntu",
  "Distribution_GENERIC_LINUX=Linux",
  "Distribution_MAC_APPLE=Apple",
  "Distribution_UNKNOWN=Unbekannt"})
public enum Distribution {
  WINDOWS_MS(Bundle.Distribution_WINDOWS_MS()),
  LINUX_REDHAT(Bundle.Distribution_LINUX_REDHAT()),
  LINUX_SUSE(Bundle.Distribution_LINUX_SUSE()),
  LINUX_RPI(Bundle.Distribution_LINUX_RPI()),
  LINUX_DEBIAN(Bundle.Distribution_LINUX_DEBIAN()),
  LINUX_UBUNTU(Bundle.Distribution_LINUX_UBUNTU()),
  GENERIC_LINUX(Bundle.Distribution_GENERIC_LINUX()),
  MAC_APPLE(Bundle.Distribution_MAC_APPLE()),
  UNKNOWN(Bundle.Distribution_UNKNOWN());
  private final String label;

  private Distribution(String label)
  {
    this.label = label;
  }

  public String getLabel()
  {
    return label;
  }

  private static Distribution testLsbRelease()
  {
    try {
      Optional<ProcessBuilder> cmd = Platform.getInstance().getCommandService().findCommand("lsb_release");

      if (cmd.isEmpty()) {
        cmd = Platform.getInstance().getCommandService().findCommand("lsb-release");
      }
      if (cmd.isEmpty()) {
        return null;
      }
      ProcessBuilder processBuilder = cmd.get();
      processBuilder.command().add("-is");
      processBuilder.redirectOutput(Redirect.PIPE);
      Process process = processBuilder.start();
      try (Reader reader = new InputStreamReader(new BufferedInputStream(process.getInputStream()))) {
        if (process.waitFor(1,
                            TimeUnit.SECONDS)) {
          if (process.exitValue() == 0) {
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[1024];
            int read = reader.read(buffer);
            if (read > 0) {
              builder.append(buffer,
                             0,
                             read);
              String desc = builder.toString().toLowerCase();
              if (desc.contains("suse")) {
                return Distribution.LINUX_SUSE;
              } else if (desc.contains("raspian")) {
                return Distribution.LINUX_RPI;
              } else if (desc.contains("red") && desc.contains("hat")) {
                return Distribution.LINUX_REDHAT;
              } else if (desc.contains("debian")) {
                return Distribution.LINUX_DEBIAN;
              } else if (desc.contains("ubuntu")) {
                return Distribution.LINUX_UBUNTU;
              } else {
                return Distribution.GENERIC_LINUX;
              }
            }
          }
        }
      }
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      Exceptions.printStackTrace(ex);
    } catch (IOException ex) {
      Exceptions.printStackTrace(ex);
    }
    return null;
  }

  private static Path findReleaseFile()
  {
    Pattern pattern = Pattern.compile(".+-release");
    Path etc = Paths.get("/etc");

    try {
      return Files.find(etc,
                        1,
                        (path, attr) -> {
                          return !attr.isDirectory() && Files.isReadable(path) && pattern.matcher(
                          path.getFileName().toString()).
                          matches();
                        }).findFirst().orElse(null);
    } catch (IOException ex) {
      Exceptions.printStackTrace(ex);
    }
    return null;
  }

  private static Distribution testStarRelease()
  {
    Path releaseFile = findReleaseFile();
    if (releaseFile == null) {
      return null;
    }
    try (LineNumberReader reader = new LineNumberReader(new FileReader(releaseFile.toFile()))) {
      String line;
      String desc = null;
      while (desc == null && (line = reader.readLine()) != null) {
        if (line.length() > 3 && line.startsWith("ID=")) {
          desc = line.substring(3).toLowerCase();
        }
      }
      if (desc != null) {
        if (desc.contains("suse")) {
          return Distribution.LINUX_SUSE;
        } else if (desc.contains("raspian")) {
          return Distribution.LINUX_RPI;
        } else if (desc.contains("red") && desc.contains("hat")) {
          return Distribution.LINUX_REDHAT;
        } else if (desc.contains("debian")) {
          return Distribution.LINUX_DEBIAN;
        } else if (desc.contains("ubuntu")) {
          return Distribution.LINUX_UBUNTU;
        } else {
          return Distribution.GENERIC_LINUX;
        }
      }
    } catch (IOException ex) {
      Exceptions.printStackTrace(ex);
    }
    return null;
  }

  public static Distribution detectDistribution()
  {
    if (OsType.isCurrent(OsType.WINDOWS)) {
      return WINDOWS_MS;
    } else if (OsType.isCurrent(OsType.LINUX)) {
      Distribution result = testLsbRelease();
      if (result == null) {
        result = testStarRelease();
      }
      if (result == null) {
        return Distribution.GENERIC_LINUX;
      } else {
        return result;
      }
    }
    return UNKNOWN;
  }

}
