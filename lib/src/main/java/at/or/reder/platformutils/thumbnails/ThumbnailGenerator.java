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
package at.or.reder.platformutils.thumbnails;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Callback interface to generate the Thumbnail Image
 */
public interface ThumbnailGenerator
{

  /**
   * Called to get the MIME Type of the original File.
   *
   * @param file the file
   * @return the MIME Type
   * @throws IOException on Error
   */
  public String getContentType(Path file) throws IOException;

  /**
   * The Dimension of the Image or Movie. Used to calculate the aspect ratio. If {@code null} returned a quadratic Image is
   * generated.
   *
   * @param file the file
   * @return Dimension or {@code null}
   * @throws IOException on Error
   */
  public Dimension getDimension(Path file) throws IOException;

  /**
   * Paint the Thumbnail.
   *
   * @param file the file
   * @param image image to paint to
   * @param meta metadata to fill the width and height of the original image.
   * @return image
   * @throws IOException on Error
   */
  public BufferedImage paintThumbnail(Path file,
                                      BufferedImage image,
                                      ThumbnailMetaData meta) throws IOException;

}
