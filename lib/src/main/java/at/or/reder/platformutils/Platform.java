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

import at.or.reder.platformutils.linux.LinuxPlatform;
import java.util.Optional;
import org.openide.util.Lookup;

public interface Platform extends Lookup.Provider {

  public static boolean isPlatformSupported()
  {
    return OsType.getCurrent().isPresent();
  }

  public static Platform getInstance()
  {
    Optional<OsType> osType = OsType.getCurrent();
    if (!osType.isPresent()) {
      throw new UnsupportedOperationException();
    }
    return switch (osType.get()) {
      case LINUX ->
        LinuxPlatform.getInstance();
      /*      case WINDOWS ->
        WindowsPlatform.getInstance();
      case MAC ->
        MacPlatform.getInstance();*/
      default ->
        throw new UnsupportedOperationException();
    };
  }

  default boolean isTrashServiceProvided()
  {
    return getTrashService() != null;
  }

  default TrashService getTrashService()
  {
    return getLookup().lookup(TrashService.class);
  }

  default boolean isPlatformFoldersProvided()
  {
    return getPlatformFolders() != null;
  }

  default PlatformFolders getPlatformFolders()
  {
    return getLookup().lookup(PlatformFolders.class);
  }

  default boolean isThumbnailServiceProvided()
  {
    return getThumbnailService() != null;
  }

  default ThumbnailService getThumbnailService()
  {
    return getLookup().lookup(ThumbnailService.class);
  }

  default Architecture getArchitecture()
  {
    return Architecture.getCurrent().orElse(null);
  }

  default OsType getOsType()
  {
    return OsType.getCurrent().orElse(null);
  }

  Distribution getDistribution();

  default boolean isCommandServiceProvided()
  {
    return getCommandService() != null;
  }

  default CommandService getCommandService()
  {
    return getLookup().lookup(CommandService.class);
  }

  Optional<String> getHostName();
}
