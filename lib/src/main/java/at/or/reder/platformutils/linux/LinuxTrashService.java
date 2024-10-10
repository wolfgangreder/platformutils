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
import at.or.reder.platformutils.TrashService;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class LinuxTrashService implements TrashService {

  private final PlatformFolders folders;
  private static final DateTimeFormatter TRASH_TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  /*
   * https://specifications.freedesktop.org/trash-spec/latest/
   */
  @Override
  public boolean moveToTrash(Path file) throws IOException
  {
    if (!Files.exists(file)) {
      return false;
    }
    if (!Files.isWritable(file)) {
      return false;
    }
    if (!Files.isExecutable(file.getParent())) {
      return false;
    }
    final BasicFileAttributes attr = Files.readAttributes(file,
                                                          BasicFileAttributes.class,
                                                          LinkOption.NOFOLLOW_LINKS);
    if (attr.isSymbolicLink()) {
      return false;
    }
    final Path normalized = file.toRealPath();
    final String strRecycleBin = folders.getTrashFolder().toString();
    final Path files = Paths.get(strRecycleBin,
                                 "files");
    final Path info = Paths.get(strRecycleBin,
                                "info");
    try (TrashContext op = findTrashFileName(normalized.getFileName().toString(),
                                             info,
                                             files)) {
      createTrashInfoFile(normalized,
                          op);
      if (attr.isDirectory()) {
        updateDirectoriesSize(file,
                              op);
      }
      Path moved = Files.move(normalized,
                              op.getNewDataFile());
      op.setSuccess(Files.exists(moved));
      return op.isSuccess();
    }
  }

  @Override
  public boolean restoreFromTrash(Path path, BiFunction<Path, Path, CollisionAction> collisionResolver) throws IOException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  private long calculateDirectorySize(Path dir) throws IOException
  {
    final LongAdder adder = new LongAdder();
    Files.walkFileTree(dir,
                       new SimpleFileVisitor<Path>() {
                 @Override
                 public FileVisitResult visitFile(Path file,
                                                  BasicFileAttributes attrs) throws IOException
                 {
                   if (attrs.isRegularFile()) {
                     adder.add(attrs.size());
                   }
                   return FileVisitResult.CONTINUE;
                 }

               });
    return adder.longValue();
  }

  private void updateDirectoriesSize(Path oldFile,
                                     TrashContext op) throws IOException
  {
    final long dirSize = calculateDirectorySize(oldFile);
    StringBuilder builder = new StringBuilder();
    builder.append(Long.toString(dirSize));
    builder.append(" ");
    FileTime fTime = Files.getLastModifiedTime(op.getInfoFile());
    builder.append(Long.toString(fTime.toMillis()));
    builder.append(" ");
    String tmp = URLEncoder.encode(oldFile.getFileName().toString(),
                                   StandardCharsets.UTF_8.name());
    builder.append(tmp);
    builder.append(System.getProperty("line.separator"));
    ByteBuffer buffer = ByteBuffer.wrap(builder.toString().getBytes(StandardCharsets.UTF_8));
    try (FileChannel channel = FileChannel.open(Paths.get(folders.getTrashFolder().toString(),
                                                          "directorysizes"),
                                                StandardOpenOption.CREATE,
                                                StandardOpenOption.APPEND,
                                                StandardOpenOption.WRITE); FileLock lock = channel.lock()) {
      channel.write(buffer);
    }
  }

  private void createTrashInfoFile(Path fileToDelete,
                                   TrashContext trashContext) throws IOException
  {
    Path info = trashContext.getInfoFile();
    try (PrintWriter writer = new PrintWriter(Files.newOutputStream(info,
                                                                    StandardOpenOption.WRITE))) {
      writer.println("[Trash Info]");
      writer.print("Path=");
      String tmp = URLEncoder.encode(fileToDelete.toString(),
                                     StandardCharsets.UTF_8.name());
      tmp = tmp.replaceAll("\\+","%20");
      writer.println(tmp);
      writer.print("DeletionDate=");
      synchronized (TRASH_TS_FORMAT) {
        tmp = TRASH_TS_FORMAT.format(LocalDateTime.now());
      }
      writer.println(tmp);
    }
  }

  private FileLock testCreateFile(Path path)
  {
    try {
      FileChannel channel = FileChannel.open(path,
                                             StandardOpenOption.WRITE,
                                             StandardOpenOption.CREATE_NEW);
      return channel.lock();
    } catch (IOException ex) {
    }
    return null;
  }

  private TrashContext findTrashFileName(String name,
                                         Path infoDir,
                                         Path filesDir) throws IOException
  {
    String strInfoDir = infoDir.toString();
    String strNewName = name;
    Path newName = Paths.get(strInfoDir,
                             strNewName + ".trashinfo");
    long ctr = 1;
    FileLock lock = null;
    while ((lock = testCreateFile(newName)) == null) {
      strNewName = name + "_" + ctr;
      newName = Paths.get(strInfoDir,
                          strNewName + ".trashinfo");
      ++ctr;
    }
    return new TrashContext(Paths.get(filesDir.toString(),
                                      strNewName),
                            lock,
                            newName);
  }
}
