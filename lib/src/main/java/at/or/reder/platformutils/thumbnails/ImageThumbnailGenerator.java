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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

/**
 * Thumbnailgenerator for still images. This class utilize ImageIO.
 */
public final class ImageThumbnailGenerator implements ThumbnailGenerator
{

  public ImageThumbnailGenerator()
  {
  }

  @Override
  public String getContentType(Path path) throws IOException
  {
    return Files.probeContentType(path);
  }

  @Override
  public Dimension getDimension(Path file) throws IOException
  {
    try (ImageInputStream iis = new FileImageInputStream(file.toFile())) {
      Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
      if (iter.hasNext()) {
        ImageReader reader = iter.next();
        try {
          reader.setInput(iis);
          int w = reader.getWidth(0);
          int h = reader.getHeight(0);
          return new Dimension(w,
                               h);
        } finally {
          reader.dispose();
        }
      }
    }
    return null;
  }

  @Override
  public BufferedImage paintThumbnail(Path file,
                                      BufferedImage thumbImg,
                                      ThumbnailMetaData meta) throws IOException
  {
    BufferedImage realImage = ImageIO.read(file.toFile());
    meta.setWidth(realImage.getWidth());
    meta.setHeight(realImage.getHeight());
    Graphics2D g = thumbImg.createGraphics();
    try {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);
      g.drawImage(realImage,
                  0,
                  0,
                  thumbImg.getWidth(),
                  thumbImg.getHeight(),
                  null);
      return thumbImg;
    } finally {
      g.dispose();
    }
  }

}
