package com.github.forax.umldoc.classfile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 *
 * Enum to define what type of uml the user wants.
 *
 */
enum UMLType {
  PLANTUML,
  MERMAID
}

/**
 *
 * A class used to read markdown file.
 *
 */
public final class MarkdownReader implements Reader {

  private int umlAnnotationLineNumber;

  /**
   *
   * Read a md file and detect the annotation for the umldoc.
   *
   * @param path File's path.
   * @throws IOException If the file is not accessible or doesn't exist.
   */
  public void readFile(Path path) throws IOException {
    Stream<String> stream = Files.lines(path);
    stream.map(String::trim)
      .filter(l -> !l.isEmpty())
      .forEach(System.out::println);
  }
}
