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

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.BiFunction;

public interface TrashService {

  enum CollisionAction {
    OVERWRITE,
    RENAME,
    CANCEL;
  }

  boolean moveToTrash(Path path) throws IOException;

  default boolean restoreFromTrash(Path path) throws IOException
  {
    return restoreFromTrash(path, (exitsting, toRestore) -> CollisionAction.CANCEL);
  }

  boolean restoreFromTrash(Path path, BiFunction<Path, Path, CollisionAction> collisionResolver) throws IOException;
}
