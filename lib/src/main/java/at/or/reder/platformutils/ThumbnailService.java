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

import at.or.reder.platformutils.thumbnails.ThumbnailGenerator;
import at.or.reder.platformutils.thumbnails.ThumbnailSize;
import java.io.IOException;
import java.nio.file.Path;

public interface ThumbnailService {

  Path getThumbnail(Path file,
                    ThumbnailSize thumbSize,
                    ThumbnailGenerator generator) throws IOException;

}
