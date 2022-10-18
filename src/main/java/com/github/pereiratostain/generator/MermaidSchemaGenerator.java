package com.github.pereiratostain.generator;

import static java.util.Objects.requireNonNull;

import com.github.forax.umldoc.core.AssociationDependency;
import com.github.forax.umldoc.core.Entity;
import com.github.forax.umldoc.core.Field;
import com.github.forax.umldoc.core.Modifier;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Generate a schema in the Mermaid format.
 */
public class MermaidSchemaGenerator implements Generator {

  @Override
  public void generate(Writer writer, List<Entity> entities, List<AssociationDependency> associations) throws IOException {
    requireNonNull(writer);
    requireNonNull(entities);
    requireNonNull(associations);

    generateHeader(writer);
    for (var entity: entities) {
      generateEntity(writer, entity);
    }
    for (var association: associations) {
      generateAssociation(association);
    }
  }

  private void generateHeader(Writer writer) throws IOException {
    writer.append("""
            classDiagram
                direction TB

            """);
  }

  private void generateEntity(Writer writer, Entity entity) throws IOException {
    var fields = generateFieldsOfEntity(entity);
    writer.append("""
                class %s {
                  %s
                  %s
                }
            """
            .formatted(entity.name(), entity.stereotype(), fields));
  }

  private String generateFieldsOfEntity(Entity entity) {
    return entity.fields().stream()
            .map(field -> applyModifiersToName(field.modifiers(), field.name()))
            .collect(Collectors.joining("\n"));
  }

  private String generateAssociation(AssociationDependency association) {
    var leftSide = association.left();
    var rightSide = association.right();
    var leftClass = leftSide.entity().name();
    var rightClass = rightSide.entity().name();
    var leftCardinality = leftSide.cardinality().name();
    var rightCardinality = rightSide.cardinality().name();
    var arrow = generateArrow(leftSide, rightSide);
    var label = getAssociationLabel(leftSide, rightSide);
    return """
            %s %s %s %s %s %s
          """
          .formatted(leftClass,
                  leftCardinality,
                  arrow,
                  rightCardinality,
                  rightClass,
                  label);
  }

  private String generateArrow(AssociationDependency.Side leftSide, AssociationDependency.Side rightSide) {
    String arrow = "";
    if (leftSide.navigability()) {
      arrow += "<";
    }
    arrow += "--";
    if (rightSide.navigability()) {
      arrow += ">";
    }
    return arrow;
  }

  private String getAssociationLabel(AssociationDependency.Side leftSide, AssociationDependency.Side rightSide) {
    var leftLabel = leftSide.label();
    var rightLabel = rightSide.label();
    if (leftLabel.isPresent() && rightLabel.isPresent()) {
      throw new IllegalStateException("Only one side of the association can hold the label");
    }
    return leftLabel.orElse(rightLabel.get());
  }

  private String computeFieldsEnum(Entity entity) {
    var fields = new ArrayList<Field>();

    for (var field : entity.fields()) {
      fields.add(field);
    }
    return generateRecordFields(fields);
  }

  private String computeFieldsClass(Entity entity, ArrayList<String> associations,
                                    Set<String> entitiesName) {
    var fields = new ArrayList<Field>();
    Pattern pattern = Pattern.compile("<.*>");

    for (var field : entity.fields()) {
      var fieldType = field.type();
      fieldType = fieldType.replace(";", "");

      Matcher matcher = pattern.matcher(fieldType);

      if (matcher.find()) {
        var string = matcher.group(0);
        string = string.replace("<", "");
        string = string.replace(">", "");
        fieldType = string;
      }

      if (entitiesName.contains(fieldType)) {
        associations.add(fieldType);
      } else {
        fields.add(field);
      }
    }

    return generateFields(fields);
  }

  private String generateRecordFields(List<Field> fields) {
    return fields.stream()
            .map(field -> "\t" + field.name())
            .collect(Collectors.joining("\n"));
  }

  private static String applyModifiersToName(Set<Modifier> modifiers, String name) {
    String prefix = "";
    String suffix = "";
    for (var modifier : modifiers) {
      switch (modifier) {
        case PUBLIC -> prefix = "+";
        case PRIVATE -> prefix = "-";
        case PROTECTED -> prefix = "#";
        case PACKAGE -> prefix = "~";
        case STATIC -> suffix = "$";
        case FINAL -> suffix = "*";
      }
    }
    return prefix + name + suffix;
  }
}
