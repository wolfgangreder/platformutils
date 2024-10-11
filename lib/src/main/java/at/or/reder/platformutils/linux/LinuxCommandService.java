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

import at.or.reder.platformutils.CommandService;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

class LinuxCommandService implements CommandService {

  @Override
  public Optional<ProcessBuilder> findCommand(String command)
  {
    Path pathToCommand = null;
    String path = System.getenv("PATH");
    if (path != null) {
      String[] paths = path.split(File.pathSeparator);
      for (String p : paths) {
        Path result = Paths.get(p,
                                command);
        if (Files.isExecutable(result)) {
          pathToCommand = result;
        }
      }
    }
    if (pathToCommand != null) {
      return Optional.of(new ProcessBuilder(command));
    }
    return Optional.empty();
  }

}
