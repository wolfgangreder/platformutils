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
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Architecture {
  AMD64("amd64"),
  ARM32("arm"),
  ARM64("aarch64");

  private final String canonicalName;

  public static Optional<Architecture> getOfCanonicalName(String name)
  {
    for (Architecture arch : values()) {
      if (arch.getCanonicalName().equalsIgnoreCase(name)) {
        return Optional.of(arch);
      }
    }
    return Optional.empty();
  }

  public static Optional<Architecture> getCurrent()
  {
    String osArch = System.getProperty("os.arch");
    Optional<Architecture> tmp = getOfCanonicalName(osArch);
    if (!tmp.isPresent()) {
      tmp = switch (osArch.toLowerCase()) {
        case "x86_64" ->
          Optional.of(AMD64);
        case "i64" ->
          Optional.of(AMD64);
        default ->
          Optional.empty();
      };
    }
    return tmp;
  }
}
