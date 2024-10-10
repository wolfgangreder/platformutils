/*
 * $Id$
 *
 * Author Wolfgang Reder (w.reder@mountain-sd.at)
 *
 * Copyright (c) 2016-2020 Mountain Software Design KG
 *
 * Diese Datei unterliegt der Mountain Software Design Sourcecode Lizenz.
 */
package at.or.reder.platformutils;

import java.nio.file.Path;
import java.nio.file.Paths;

public interface PlatformFolders {

  Path getTrashFolder();

  Path getUserHome();

  Path getDownloadsFolder();

  Path getDesktopFolder();

  Path getDokumentsFolder();

  Path getPicturesFolder();

  Path getVideosFolder();

  Path getMusicFolder();

  Path getPublicDownloadsFolder();

  Path getPublicDesktopFolder();

  Path getPublicDokumentsFolder();

  Path getPublicPicturesFolder();

  Path getPublicVideosFolder();

  Path getPublicMusicFolder();

  Path getCacheFolder();

  default Path getTmpPath()
  {
    return Paths.get(System.getProperty("java.io.tmpdir"));
  }

  Path getThumbnailFolder();

/*  public abstract Path findCommand(String command);

  public abstract String getHostName();

  public String execReadToString(String execCommand) throws IOException
  {
    try (Scanner s = new Scanner(Runtime.getRuntime().exec(execCommand).getInputStream()).useDelimiter("\\A")) {
      return s.hasNext() ? s.next() : "";
    }
  }*/

}
