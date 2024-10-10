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

import java.util.Optional;

public enum OsType {
  LINUX,
  MAC,
  WINDOWS,
  ANDROID;

  public static Optional<OsType> getCurrent()
  {
    String osName = System.getProperty("os.name").toLowerCase();
    if (osName.startsWith("linux")) {
      if ("dalvik".equals(System.getProperty("java.vm.name").toLowerCase())) {
        return Optional.of(ANDROID);
      } else {
        return Optional.of(LINUX);
      }
    } else if (osName.startsWith("windows")) {
      return Optional.of(WINDOWS);
    } else if (osName.startsWith("max") || osName.startsWith("darwin")) {
      return Optional.of(MAC);
    } else {
      return Optional.empty();
    }
  }

  public static boolean isCurrent(OsType osType)
  {
    return getCurrent().filter(os -> os == osType).isPresent();
  }
}
