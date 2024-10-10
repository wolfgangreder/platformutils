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
package at.or.reder.platformutils.util;

import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LazyInitialized<T> implements Supplier<T> {

  private final Supplier<T> initializer;
  private Optional<T> value;

  private Optional<T> ensureCreated()
  {
    synchronized (this) {
      if (value != null) {
        return value;
      }
    }
    T tmp = initializer.get();
    synchronized (this) {
      if (value == null) {
        value = Optional.ofNullable(tmp);
      }
      return value;
    }
  }

  public Optional<T> getOptional()
  {
    return ensureCreated();
  }

  @Override
  public T get()
  {
    return ensureCreated().orElse(null);
  }
}
