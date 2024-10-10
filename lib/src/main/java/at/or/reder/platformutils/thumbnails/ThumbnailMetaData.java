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

import java.net.URI;
import java.util.Set;

/**
 * Thumbnail Metadata. 
 * See<a href="https://specifications.freedesktop.org/thumbnail-spec/latest.html">Freedesktop Thumbnail Specification</a>.
 */
public interface ThumbnailMetaData {

  public static final String THUMB_URI = "Thumb::URI";
  public static final String THUMB_MTIME = "Thumb::MTime";
  public static final String THUMB_SIZE = "Thumb::Size";
  public static final String THUMB_MIMETYPE = "Thumb::Mimetype";
  public static final String THUMB_WIDTH = "Thumb::Image::Width";
  public static final String THUMB_HEIGHT = "Thumb::Image::Height";
  public static final String THUMB_PAGES = "Thumb::Document::Pages";
  public static final String THUMB_LENGTH = "Thumb::Movie::Length";
  public static final String THUMB_DESCRIPTION = "Description";
  public static final String THUMB_SOFTWARE = "Software";
  public static final Set<String> META_NAMES = Set.of(THUMB_DESCRIPTION,
          THUMB_HEIGHT,
          THUMB_LENGTH,
          THUMB_MIMETYPE,
          THUMB_MTIME,
          THUMB_PAGES,
          THUMB_SIZE,
          THUMB_SOFTWARE,
          THUMB_URI,
          THUMB_WIDTH);

  public URI getUri();

  public void setUri(URI uri);

  public long getMTime();

  public void setMTime(long mTime);

  public long getSize();

  public void setSize(long size);

  public String getMime();

  public void setMime(String mime);

  public int getWidth();

  public void setWidth(int width);

  public int getHeight();

  public void setHeight(int height);

  public int getPages();

  public void setPages(int pages);

  public long getLength();

  public void setLength(long length);

  public String getSoftware();

  public void setSoftware(String software);

  public String getDescription();

  public void setDescription(String description);

}
