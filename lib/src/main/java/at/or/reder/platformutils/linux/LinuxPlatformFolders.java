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
import at.or.reder.platformutils.util.LazyInitialized;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class LinuxPlatformFolders implements PlatformFolders {

  private final LazyInitialized<Path> publicShare = new LazyInitialized(() -> loadPathFromUserDir("XDG_PUBLICSHARE_DIR"));
  private final LazyInitialized<Path> home = new LazyInitialized(() -> Paths.get(System.getProperty("user.home")));
  private final LazyInitialized<Path> recyclebin = new LazyInitialized(this::createRecycleBinPath);
  private final LazyInitialized<Path> downloads = new LazyInitialized(() -> loadPathFromUserDir("XDG_DOWNLOAD_DIR"));
  private final LazyInitialized<Path> desktop = new LazyInitialized(() -> loadPathFromUserDir("XDG_DESKTOP_DIR"));
  private final LazyInitialized<Path> documents = new LazyInitialized(() -> loadPathFromUserDir("XDG_DOCUMENTS_DIR"));
  private final LazyInitialized<Path> pictures = new LazyInitialized(() -> loadPathFromUserDir("XDG_PICTURES_DIR"));
  private final LazyInitialized<Path> videos = new LazyInitialized(() -> loadPathFromUserDir("XDG_VIDEOS_DIR"));
  private final LazyInitialized<Path> music = new LazyInitialized(() -> loadPathFromUserDir("XDG_MUSIC_DIR"));
  private final LazyInitialized<Path> cacheDir = new LazyInitialized(this::getCacheRoot);
  private final LazyInitialized<Path> thumbnailDir = new LazyInitialized(() -> loadThumbnailDir());

  private Path getEnvOrDefault(String env,
                               String defaultValue)
  {
    String tmp = System.getenv(env);
    if (tmp == null || tmp.isEmpty()) {
      tmp = defaultValue;
    }
    if (tmp != null) {
      tmp = tmp.replaceAll("\\$HOME",
                           System.getProperty("user.home"));
      return Paths.get(tmp);
    }
    return null;
  }

  private Path getDataHome()
  {
    return getEnvOrDefault("XDG_DATA_HOME",
                           "$HOME" + File.separator + ".local" + File.separator + "share");
  }

  private Path getConfigHome()
  {
    return getEnvOrDefault("XDG_CONFIG_HOME",
                           "$HOME" + File.separator + ".config");
  }

  private Path getCacheRoot()
  {
    return getEnvOrDefault("XDG_CACHE_HOME",
                           "$HOME" + File.separator + ".cache");

  }

  @Override
  public Path getThumbnailFolder()
  {
    return thumbnailDir.get();
  }

  private Path loadThumbnailDir()
  {
    Path cacheRoot = getEnvOrDefault("XDG_CACHE_HOME",
                                     null);
    if (cacheRoot == null) {
      Path suseCandicate = Paths.get(getUserHome().toString(),
                                     ".thumbnails");
      if (Files.isDirectory(suseCandicate)) {
        Path sub = Paths.get(suseCandicate.toString(),
                             "normal");
        int subCounter = 0;
        if (Files.isDirectory(sub)) {
          ++subCounter;
        }
        sub = Paths.get(suseCandicate.toString(),
                        "large");
        if (Files.isDirectory(sub)) {
          ++subCounter;
        }
        if (subCounter > 0) {
          return suseCandicate;
        }
      }
      return Paths.get(getUserHome().toString(),
                       ".cache",
                       "thumbnails");
    } else {
      return Paths.get(cacheRoot.toString(),
                       "thumbnails");
    }
  }

  private Path createRecycleBinPath()
  {
    return Paths.get(getDataHome().toString(),
                     "Trash");
  }

  private Path loadPathFromUserDir(String folderId)
  {
    String strHome = home.get().toString().trim();
    Path path = Paths.get(getConfigHome().toString(),
                          "user-dirs.dirs");
    try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(Files.newInputStream(path)))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.startsWith(folderId)) {
          int equalPos = line.indexOf('=');
          if (equalPos > 0) {
            String tmp = line.substring(equalPos + 1);
            tmp = tmp.replaceAll("\"",
                                 "");
            tmp = tmp.replaceAll("\\$HOME",
                                 strHome);
            return Paths.get(tmp);
          }
        }
      }
    } catch (IOException ex) {

    }
    return loadPathFromUserDirDefaults(folderId,
                                       strHome);
  }

  private Path loadPathFromUserDirDefaults(String folderId,
                                           String strHome)
  {
    Path path = Paths.get(File.separator + "etc",
                          "xdg",
                          "user-dirs.defaults");
    String transformed = folderId.replaceAll("(XDG_)|(_DIR)",
                                             "");
    try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(Files.newInputStream(path)))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.startsWith(transformed)) {
          int equalPos = line.indexOf('=');
          if (equalPos > 0) {
            String tmp = line.substring(equalPos + 1);
            tmp = tmp.replaceAll("\"",
                                 "");
            return Paths.get(strHome,
                             tmp);
          }
        }
      }
    } catch (IOException ex) {

    }
    return null;
  }

  @Override
  public Path getTrashFolder()
  {
    return recyclebin.get();
  }

  @Override
  public Path getUserHome()
  {
    return home.get();
  }

  @Override
  public Path getDownloadsFolder()
  {
    return downloads.get();
  }

  @Override
  public Path getDesktopFolder()
  {
    return desktop.get();
  }

  @Override
  public Path getDokumentsFolder()
  {
    return documents.get();
  }

  @Override
  public Path getPicturesFolder()
  {
    return pictures.get();
  }

  @Override
  public Path getVideosFolder()
  {
    return videos.get();
  }

  @Override
  public Path getMusicFolder()
  {
    return music.get();
  }

  @Override
  public Path getPublicDownloadsFolder()
  {
    return publicShare.get();
  }

  @Override
  public Path getPublicDesktopFolder()
  {
    return publicShare.get();
  }

  @Override
  public Path getPublicDokumentsFolder()
  {
    return publicShare.get();
  }

  @Override
  public Path getPublicPicturesFolder()
  {
    return publicShare.get();
  }

  @Override
  public Path getPublicVideosFolder()
  {
    return publicShare.get();
  }

  @Override
  public Path getPublicMusicFolder()
  {
    return publicShare.get();
  }

  @Override
  public Path getCacheFolder()
  {
    return cacheDir.get();
  }

}
