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

import at.or.reder.platformutils.PlatformFolders;
import at.or.reder.platformutils.ThumbnailService;
import at.or.reder.platformutils.thumbnails.KDEThumbnailMetaData;
import at.or.reder.platformutils.thumbnails.ThumbnailGenerator;
import at.or.reder.platformutils.thumbnails.ThumbnailSize;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.logging.Level;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@RequiredArgsConstructor
@Log
class LinuxThumbnailService implements ThumbnailService {

  private final PlatformFolders folders;

  @Override
  public Path getThumbnail(Path fileIn, ThumbnailSize thumbSize, ThumbnailGenerator generator) throws IOException
  {
    final Path file = fileIn.toRealPath().toAbsolutePath();
    if (thumbSize == null) {
      thumbSize = ThumbnailSize.LARGE;
    }
    if (!Files.isRegularFile(file)) {
      throw new IOException(file.toString() + " is not a regular file");
    }
    if (!Files.isReadable(file)) {
      throw new IOException(file.toString() + " is not readable");
    }
    String thumbFileName = createThumbFileName(file);
    Path thumbDir = Paths.get(folders.getThumbnailFolder().toString(),
                              thumbSize.getSubfolder());
    Files.createDirectories(thumbDir);
    Path thumbFilePath = Paths.get(thumbDir.toString(),
                                   thumbFileName);
    KDEThumbnailMetaData meta = null;
    final FileTime fTime = Files.getLastModifiedTime(file);
    if (Files.isReadable(thumbFilePath)) {
      try (ImageInputStream iis = new FileImageInputStream(thumbFilePath.toFile())) {
        Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
        if (iter.hasNext()) {
          ImageReader reader = iter.next();
          try {
            reader.setInput(iis);
            meta = KDEThumbnailMetaData.fromMeta(reader.getImageMetadata(0));
          } finally {
            reader.dispose();
          }
        } else { // kein lesbares format damit auch kein PNG
          Files.delete(thumbFilePath);
        }
      }
    }
    if (meta != null) {
      if (meta.getMTime() != fTime.toMillis() / 1000) {
        meta = null;
      }
    }
    if (meta != null) {
      return thumbFilePath;
    }
    Path tmpFile = Files.createTempFile(thumbDir,
                                        "tmpThumb",
                                        ".png");
    Dimension dim = generator.getDimension(file);
    final int size = thumbSize.getDim();
    if (dim != null) {
      double scaleX = size / dim.getWidth();
      double scaleY = size / dim.getHeight();
      double scale = Math.min(scaleY,
                              scaleX);
      dim = new Dimension((int) (dim.getWidth() * scale),
                          (int) (dim.getHeight() * scale));
    } else {
      dim = new Dimension(size,
                          size);
    }
    BufferedImage img = new BufferedImage(dim.width,
                                          dim.height,
                                          BufferedImage.TYPE_4BYTE_ABGR);
    final String contentType = generator.getContentType(file);
    final ImageWriter writer = getPNGImageWriter();
    try {
      try (ImageOutputStream ios = new FileImageOutputStream(tmpFile.toFile())) {
        writer.setOutput(ios);
        ImageWriteParam writeParam = writer.getDefaultWriteParam();
        if (writeParam.canWriteProgressive()) {
          writeParam.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
        }
        ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_4BYTE_ABGR);
        IIOMetadata imeta = writer.getDefaultImageMetadata(typeSpecifier,
                                                           writeParam);
        meta = new KDEThumbnailMetaData();
        meta.setMTime(fTime.toMillis() / 1000);
        meta.setMime(contentType);
        meta.setSize(Files.size(file));
        meta.setUri(file.toUri());
        img = generator.paintThumbnail(file,
                                       img,
                                       meta);
        meta.toMeta(imeta);
        writer.write(imeta,
                     new IIOImage(img,
                                  null,
                                  imeta),
                     writeParam);
      } finally {
        writer.dispose();
      }
      Files.move(tmpFile,
                 thumbFilePath,
                 StandardCopyOption.ATOMIC_MOVE,
                 StandardCopyOption.REPLACE_EXISTING);
      Files.setPosixFilePermissions(thumbFilePath,
                                    EnumSet.of(PosixFilePermission.OWNER_READ,
                                               PosixFilePermission.OWNER_WRITE));
    } finally {
      Files.deleteIfExists(tmpFile);
    }
    return thumbFilePath;
  }

  private String createThumbFileName(Path file) throws IOException
  {
    try {
      MessageDigest digest = MessageDigest.getInstance("MD5");
      ByteBuffer bb = ByteBuffer.wrap(digest.digest(file.toUri().toASCIIString().getBytes(StandardCharsets.UTF_8)));
      StringBuilder builder = new StringBuilder();
      String tmp = Long.toHexString(bb.getLong());
      for (int i = tmp.length(); i < 16; ++i) {
        builder.append('0');
      }
      builder.append(tmp);
      tmp = Long.toHexString(bb.getLong());
      for (int i = tmp.length(); i < 16; ++i) {
        builder.append('0');
      }
      builder.append(tmp);
      return builder.append(".png").toString();
    } catch (NoSuchAlgorithmException ex) {
      log.log(Level.SEVERE,
              null,
              ex);
    }
    return null;
  }

  private boolean canWriteCompressed(ImageWriter w)
  {
    ImageWriteParam p = w.getDefaultWriteParam();
    return p.canWriteCompressed();
  }

  private ImageWriter getPNGImageWriter()
  {
    Iterator<ImageWriter> iter = ImageIO.getImageWritersByMIMEType("image/png");
    ImageWriter candidate = null;
    while (iter.hasNext()) {
      ImageWriter w = iter.next();
      if (candidate == null) {
        candidate = w;
      } else if (canWriteCompressed(w)) {
        candidate = w;
      }
      if (canWriteCompressed(candidate)) {
        return candidate;
      }
    }
    return candidate;
  }
}
