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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class KDEThumbnailMetaData implements ThumbnailMetaData {

  private URI uri;
  private long mTime;
  private long size;
  private String mime;
  private int width;
  private int height;
  private int pages;
  private long length;
  private String software;
  private String description;

  @SuppressWarnings("UseSpecificCatch")
  private static int getInt(Map<String, String> m,
                            String key)
  {
    String tmp = m.get(key);
    if (tmp != null) {
      try {
        return Integer.parseInt(tmp);
      } catch (Throwable th) {
      }
    }
    return 0;
  }

  @SuppressWarnings("UseSpecificCatch")
  private static long getLong(Map<String, String> m,
                              String key)
  {
    String tmp = m.get(key);
    if (tmp != null) {
      try {
        return Long.parseLong(tmp);
      } catch (Throwable th) {
      }
    }
    return 0;
  }

  @SuppressWarnings("UseSpecificCatch")
  public static KDEThumbnailMetaData fromMap(Map<String, String> map)
  {
    KDEThumbnailMetaData meta = new KDEThumbnailMetaData();
    meta.setDescription(map.get(THUMB_DESCRIPTION));
    meta.setHeight(getInt(map,
                          THUMB_HEIGHT));
    meta.setLength(getLong(map,
                           THUMB_LENGTH));
    meta.setMime(map.get(THUMB_MIMETYPE));
    meta.setPages(getInt(map,
                         THUMB_PAGES));
    meta.setSize(getLong(map,
                         THUMB_SIZE));
    meta.setSoftware(map.get(THUMB_SOFTWARE));
    meta.setWidth(getInt(map,
                         THUMB_WIDTH));
    meta.setMTime(getLong(map,
                          THUMB_MTIME));
    String tmp = map.get(THUMB_URI);
    if (tmp != null) {
      try {
        URI u = new URI(tmp);
        meta.setUri(u);
      } catch (Throwable th) {
      }
    }
    return meta;
  }

  private static List<Node> findNode(Node parent,
                                     String nodeName)
  {
    NodeList nl = parent.getChildNodes();
    List<Node> result = new ArrayList<>();
    for (int i = 0; i < nl.getLength(); ++i) {
      Node n = nl.item(i);
      if (n.getNodeName().equals(nodeName)) {
        result.add(n);
      }
    }
    return result;
  }

  private static Node findFirstNode(Node meta,
                                    String name)
  {
    List<Node> tmp = findNode(meta,
                              name);
    if (tmp.isEmpty()) {
      return null;
    }
    return tmp.get(0);
  }

  private static Map<String, String> getThumbAttributes(Node meta)
  {
    Map<String, String> result = new HashMap<>();
    Node text = findFirstNode(meta,
                              "tEXt");
    if (text != null) {
      List<Node> elements = findNode(text,
                                     "tEXtEntry");
      for (Node n : elements) {
        NamedNodeMap map = n.getAttributes();
        Node attr = map.getNamedItem("keyword");
        String metaName = attr.getNodeValue();
        if (META_NAMES.contains(metaName)) {
          Node value = map.getNamedItem("value");
          if (value != null) {
            result.put(metaName,
                       value.getNodeValue());
          } else {
            result.put(metaName,
                       null);
          }
        }
      }
    }
    return result;
  }

  public static KDEThumbnailMetaData fromMeta(IIOMetadata meta)
  {
    String nativeName = meta.getNativeMetadataFormatName();
    Node root = meta.getAsTree(nativeName);
    return fromMap(getThumbAttributes(root));
  }

  public void toMeta(IIOMetadata meta) throws IIOInvalidTreeException
  {
    Map<String, String> map = toMap();
    String formatName = meta.getNativeMetadataFormatName();
    Node root = meta.getAsTree(formatName);
    Node text = findFirstNode(root,
                              "tEXt");
    if (text == null) {
      text = new IIOMetadataNode("tEXt");
      root.appendChild(text);
    }
    for (Map.Entry<String, String> e : map.entrySet()) {
      IIOMetadataNode n = new IIOMetadataNode("tEXtEntry");
      text.appendChild(n);
      n.setAttribute("keyword",
                     e.getKey());
      n.setAttribute("value",
                     e.getValue());
    }
    meta.mergeTree(formatName,
                   root);
  }

  public Map<String, String> toMap()
  {
    Map<String, String> result = new HashMap<>();
    if (uri != null) {
      result.put(THUMB_URI,
                 uri.toString());
    }
    if (description != null) {
      result.put(THUMB_DESCRIPTION,
                 description);
    }
    if (height > 0) {
      result.put(THUMB_HEIGHT,
                 Integer.toString(height));
    }
    if (length > 0) {
      result.put(THUMB_LENGTH,
                 Long.toString(length));
    }
    if (mime != null) {
      result.put(THUMB_MIMETYPE,
                 mime);
    }
    if (mTime > 0) {
      result.put(THUMB_MTIME,
                 Long.toString(mTime));
    }
    if (pages > 0) {
      result.put(THUMB_PAGES,
                 Integer.toString(pages));
    }
    if (size > 0) {
      result.put(THUMB_SIZE,
                 Long.toString(size));
    }
    if (software != null) {
      result.put(THUMB_SOFTWARE,
                 software);
    }
    if (width != 0) {
      result.put(THUMB_WIDTH,
                 Integer.toString(width));
    }
    return result;
  }

  @Override
  public URI getUri()
  {
    return uri;
  }

  @Override
  public void setUri(URI uri)
  {
    this.uri = uri;
  }

  @Override
  public long getMTime()
  {
    return mTime;
  }

  @Override
  public void setMTime(long mTime)
  {
    this.mTime = mTime;
  }

  @Override
  public long getSize()
  {
    return size;
  }

  @Override
  public void setSize(long size)
  {
    this.size = size;
  }

  @Override
  public String getMime()
  {
    return mime;
  }

  @Override
  public void setMime(String mime)
  {
    this.mime = mime;
  }

  @Override
  public int getWidth()
  {
    return width;
  }

  @Override
  public void setWidth(int width)
  {
    this.width = width;
  }

  @Override
  public int getHeight()
  {
    return height;
  }

  @Override
  public void setHeight(int height)
  {
    this.height = height;
  }

  @Override
  public int getPages()
  {
    return pages;
  }

  @Override
  public void setPages(int pages)
  {
    this.pages = pages;
  }

  @Override
  public long getLength()
  {
    return length;
  }

  @Override
  public void setLength(long length)
  {
    this.length = length;
  }

  @Override
  public String getSoftware()
  {
    return software;
  }

  @Override
  public void setSoftware(String software)
  {
    this.software = software;
  }

  @Override
  public String getDescription()
  {
    return description;
  }

  @Override
  public void setDescription(String description)
  {
    this.description = description;
  }

}
