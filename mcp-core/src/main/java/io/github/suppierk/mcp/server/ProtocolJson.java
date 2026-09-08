package io.github.suppierk.mcp.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.suppierk.mcp.protocol.McpCreateMessageResult;
import io.github.suppierk.mcp.protocol.McpElicitResult;
import io.github.suppierk.mcp.protocol.McpInputRequests;
import io.github.suppierk.mcp.protocol.McpInputResponse;
import io.github.suppierk.mcp.protocol.McpInputResponses;
import io.github.suppierk.mcp.protocol.McpJsonNull;
import io.github.suppierk.mcp.protocol.McpListRootsResult;
import io.github.suppierk.mcp.protocol.McpMetaObject;
import io.github.suppierk.mcp.protocol.McpRequestMetaObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.PropertyName;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.deser.Deserializers;
import tools.jackson.databind.deser.jdk.UntypedObjectDeserializer;
import tools.jackson.databind.ext.jdk8.Jdk8OptionalDeserializer;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.AnnotatedClass;
import tools.jackson.databind.introspect.AnnotatedConstructor;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.introspect.AnnotatedParameter;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;
import tools.jackson.databind.jsontype.TypeDeserializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.type.ReferenceType;

/** Keeps protocol wire names and scalar mappings inside the JSON implementation. */
final class ProtocolJson extends JacksonAnnotationIntrospector {
  static final ProtocolJson INSTANCE = new ProtocolJson();
  private static final long serialVersionUID = 1L;
  private static final String PROTOCOL_PACKAGE = "io.github.suppierk.mcp.protocol";
  private static final Set<String> CONSTANT_PROPERTIES =
      Set.of("jsonrpc", "method", "type", "code", "mode");
  private static final Set<Class<?>> VALUE_WRAPPERS =
      Set.of(McpMetaObject.class, McpInputRequests.class, McpInputResponses.class);

  /** Creates the shared stateless mapping. */
  private ProtocolJson() {}

  /** Preserves explicit JSON null independently of absent optional members. */
  static SimpleModule nullValues() {
    var module =
        new SimpleModule("mcp-json-values") {
          /** Registers the optional mapping before Jackson's built-in reference shortcut. */
          @Override
          public void setupModule(SetupContext context) {
            super.setupModule(context);
            context.addDeserializers(
                new Deserializers.Base() {
                  /** Only optional references need this specialized mapping. */
                  @Override
                  public boolean hasDeserializerFor(DeserializationConfig config, Class<?> type) {
                    return type == Optional.class;
                  }

                  /** Keeps omitted members empty without losing explicit null values. */
                  @Override
                  public ValueDeserializer<?> findReferenceDeserializer(
                      ReferenceType type,
                      DeserializationConfig config,
                      BeanDescription.Supplier bean,
                      TypeDeserializer types,
                      ValueDeserializer<?> values) {
                    return type.hasRawClass(Optional.class)
                        ? new OptionalValues(type, types, values)
                        : null;
                  }
                });
          }
        };
    module.setMixInAnnotation(McpInputResponse.class, InputResponseTypes.class);
    module.addSerializer(
        McpJsonNull.class,
        new ValueSerializer<McpJsonNull>() {
          /** Writes the JSON null token rather than the enum name. */
          @Override
          public void serialize(
              McpJsonNull value, JsonGenerator output, SerializationContext provider) {
            output.writeNull();
          }
        });
    module.addDeserializer(
        Object.class,
        new UntypedObjectDeserializer((JavaType) null, null) {
          /** Keeps explicit null present in an optional arbitrary-JSON member. */
          @Override
          public Object getNullValue(DeserializationContext context) {
            return McpJsonNull.INSTANCE;
          }

          /** Leaves a missing required member for the record constructor to reject. */
          @Override
          public Object getAbsentValue(DeserializationContext context) {
            return null;
          }
        });
    return module;
  }

  /** Selects the three input result shapes without adding wire tags or public annotations. */
  @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
  @JsonSubTypes({
    @JsonSubTypes.Type(McpElicitResult.class),
    @JsonSubTypes.Type(McpCreateMessageResult.class),
    @JsonSubTypes.Type(McpListRootsResult.class)
  })
  private interface InputResponseTypes {}

  /** Preserves the protocol's empty-optional contract through contextual deserialization. */
  private static final class OptionalValues extends Jdk8OptionalDeserializer {
    /** Creates the reference deserializer with its resolved content mapping. */
    private OptionalValues(
        JavaType type, TypeDeserializer typeDeserializer, ValueDeserializer<?> values) {
      super(type, null, typeDeserializer, values);
    }

    /** Retains absent-value handling when Jackson resolves the content type. */
    @Override
    public Jdk8OptionalDeserializer withResolved(
        TypeDeserializer types, ValueDeserializer<?> values) {
      return new OptionalValues(_fullType, types, values);
    }

    /** An omitted member is empty, even when explicit null has a sentinel value. */
    @Override
    public Object getAbsentValue(DeserializationContext context) {
      return Optional.empty();
    }
  }

  /** Converts an internal tree into JDK values before it enters a public model. */
  static Object value(Object supplied) {
    if (!(supplied instanceof JsonNode node)) {
      return supplied;
    }
    if (node.isNull()) {
      return McpJsonNull.INSTANCE;
    }
    if (node.isObject()) {
      Map<String, Object> values = new LinkedHashMap<>();
      node.properties()
          .forEach(
              entry ->
                  values.put(
                      entry.getKey(), entry.getValue().isNull() ? null : value(entry.getValue())));
      return values;
    }
    if (node.isArray()) {
      var values = new ArrayList<>();
      node.forEach(entry -> values.add(entry.isNull() ? null : value(entry)));
      return values;
    }
    if (node.isString()) {
      return node.stringValue();
    }
    if (node.isBoolean()) {
      return node.booleanValue();
    }
    if (node.isNumber()) {
      return node.numberValue();
    }
    throw new IllegalArgumentException("Unsupported internal JSON value");
  }

  /** Converts an internal JSON object to a public JDK object value. */
  @SuppressWarnings("unchecked")
  static Map<String, ?> object(JsonNode node) {
    if (!node.isObject()) {
      throw new IllegalArgumentException("A JSON object is required");
    }
    return (Map<String, ?>) value(node);
  }

  /** Supplies protocol-specific names without annotations on public records. */
  @Override
  public PropertyName findNameForSerialization(MapperConfig<?> config, Annotated value) {
    PropertyName name = wireName(value);
    return name == null ? super.findNameForSerialization(config, value) : name;
  }

  /** Uses the same names for constructor arguments and encoded properties. */
  @Override
  public PropertyName findNameForDeserialization(MapperConfig<?> config, Annotated value) {
    PropertyName name = wireName(value);
    return name == null ? super.findNameForDeserialization(config, value) : name;
  }

  /** Writes the entries of the protocol's map wrappers directly. */
  @Override
  public Boolean hasAsValue(MapperConfig<?> config, Annotated value) {
    if (value instanceof AnnotatedMember member
        && VALUE_WRAPPERS.contains(member.getDeclaringClass())
        && value.getName().equals("values")) {
      return true;
    }
    return super.hasAsValue(config, value);
  }

  /** Flattens application-defined request metadata on output. */
  @Override
  public Boolean hasAnyGetter(MapperConfig<?> config, Annotated value) {
    return isExtensions(value) ? Boolean.TRUE : super.hasAnyGetter(config, value);
  }

  /** Captures application-defined request metadata on input. */
  @Override
  public Boolean hasAnySetter(MapperConfig<?> config, Annotated value) {
    return isExtensions(value) ? Boolean.TRUE : super.hasAnySetter(config, value);
  }

  /** Decodes generic metadata directly from its JSON object. */
  @Override
  public JsonCreator.Mode findCreatorAnnotation(MapperConfig<?> config, Annotated value) {
    if (value instanceof AnnotatedConstructor constructor
        && constructor.getDeclaringClass() == McpMetaObject.class) {
      return JsonCreator.Mode.DELEGATING;
    }
    return super.findCreatorAnnotation(config, value);
  }

  /** Uses the protocol's lowercase role and severity names. */
  @Override
  public String[] findEnumValues(
      MapperConfig<?> config, AnnotatedClass type, Enum<?>[] values, String[] names) {
    if (type.getRawType().getPackageName().equals(PROTOCOL_PACKAGE)) {
      for (int index = 0; index < values.length; index++) {
        names[index] = values[index].name().toLowerCase(Locale.ROOT);
      }
      return names;
    }
    return super.findEnumValues(config, type, values, names);
  }

  /** Finds a wire name only for a member of a protocol record. */
  private static PropertyName wireName(Annotated value) {
    if (!(value instanceof AnnotatedMember member)
        || !member.getDeclaringClass().isRecord()
        || !member.getDeclaringClass().getPackageName().equals(PROTOCOL_PACKAGE)) {
      return null;
    }
    String name = memberName(member);
    String type = member.getDeclaringClass().getSimpleName();
    if (name.equals("meta")) {
      return PropertyName.construct("_meta");
    }
    if (name.equals("defaultValue")) {
      return PropertyName.construct("default");
    }
    if (name.equals("values")
        && (type.equals("McpUntitledSingleSelectEnumSchema")
            || type.equals("McpLegacyTitledEnumSchema"))) {
      return PropertyName.construct("enum");
    }
    if (type.endsWith("MetaObject")
        && Set.of(
                "clientCapabilities",
                "clientInfo",
                "logLevel",
                "protocolVersion",
                "serverInfo",
                "subscriptionId")
            .contains(name)) {
      return PropertyName.construct("io.modelcontextprotocol/" + name);
    }
    return CONSTANT_PROPERTIES.contains(name) ? PropertyName.construct(name) : null;
  }

  /** Identifies the extension member and its canonical constructor argument. */
  private static boolean isExtensions(Annotated value) {
    return value instanceof AnnotatedMember member
        && member.getDeclaringClass() == McpRequestMetaObject.class
        && memberName(member).equals("extensions");
  }

  /** Gets record-component names for canonical constructor parameters. */
  private static String memberName(AnnotatedMember member) {
    if (member instanceof AnnotatedParameter parameter
        && parameter.getOwner() instanceof AnnotatedConstructor constructor
        && constructor.getDeclaringClass().isRecord()
        && constructor.getParameterCount()
            == constructor.getDeclaringClass().getRecordComponents().length) {
      return constructor.getDeclaringClass().getRecordComponents()[parameter.getIndex()].getName();
    }
    return member.getName();
  }
}
