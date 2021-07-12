package com.tenxerconsulting.swagger.doclet.parser;

import static com.tenxerconsulting.swagger.doclet.parser.ParserHelper.createRef;

import io.swagger.oas.models.media.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class SchemaBuilder {

    /**
     * Creates new property on the passed arguments.
     *
     * @param type   property type
     * @param format property format
     * @param args   mapping of argument identifier to value
     * @return new property instance or <code>null</code> for unknown types
     */
    public static Schema build(String type, String format, Map<PropertyId, Object> args) {
        final Processor processor = Processor.fromType(type, format);
        if (processor == null) {
            return null;
        }
        final Map<PropertyId, Object> safeArgs = args == null ? Collections.emptyMap() : args;
        final Map<PropertyId, Object> fixedArgs;
        if (format != null) {
            fixedArgs = new EnumMap<>(PropertyId.class);
            fixedArgs.putAll(safeArgs);
            fixedArgs.put(PropertyId.FORMAT, format);
        } else {
            fixedArgs = safeArgs;
        }
        return processor.build(fixedArgs);
    }

    /**
     * Merges passed arguments into an existing property instance.
     *
     * @param schema property to be updated
     * @param args     mapping of argument identifier to value. <code>null</code>s
     *                 will replace existing values
     * @return updated property instance
     */
    public static Schema merge(Schema schema, Map<PropertyId, Object> args) {
        if (args != null && !args.isEmpty()) {
            final Processor processor = Processor.fromProperty(schema);
            if (processor != null) {
                processor.merge(schema, args);
            }
        }
        return schema;
    }

    /**
     * Converts passed property into a model.
     *
     * @param schema property to be converted
     * @return model instance or <code>null</code> for unknown types
     */
    public static Schema toModel(Schema schema) {
        final Processor processor = Processor.fromProperty(schema);
        if (processor != null) {
            return processor.toModel(schema);
        }
        return null;
    }

    public enum PropertyId {
        ENUM("enum"),
        TITLE("title"),
        DESCRIPTION("description"),
        DEFAULT("default"),
        PATTERN("pattern"),
        DESCRIMINATOR("discriminator"),
        MIN_ITEMS("minItems"),
        MAX_ITEMS("maxItems"),
        MIN_PROPERTIES("minProperties"),
        MAX_PROPERTIES("maxProperties"),
        MIN_LENGTH("minLength"),
        MAX_LENGTH("maxLength"),
        MINIMUM("minimum"),
        MAXIMUM("maximum"),
        EXCLUSIVE_MINIMUM("exclusiveMinimum"),
        EXCLUSIVE_MAXIMUM("exclusiveMaximum"),
        UNIQUE_ITEMS("uniqueItems"),
        EXAMPLE("example"),
        TYPE("type"),
        FORMAT("format"),
        READ_ONLY("readOnly"),
        REQUIRED("required"),
        VENDOR_EXTENSIONS("vendorExtensions");

        private String propertyName;

        private PropertyId(String propertyName) {
            this.propertyName = propertyName;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public <T> T findValue(Map<PropertyId, Object> args) {
            @SuppressWarnings("unchecked")
            final T value = (T) args.get(this);
            return value;
        }
    }

    private enum Processor {
        BOOLEAN(BooleanSchema.class) {
            @Override
            protected boolean isType(String type, String format) {
                return "boolean".equals(type);
            }

            @Override
            protected BooleanSchema create() {
                return new BooleanSchema();
            }

            @Override
            public Schema merge(Schema property, Map<PropertyId, Object> args) {
                super.merge(property, args);
                if (property instanceof BooleanSchema) {
                    final BooleanSchema resolved = (BooleanSchema) property;
                    if (args.containsKey(PropertyId.DEFAULT)) {
                        final String value = PropertyId.DEFAULT.findValue(args);
                        if (value != null) {
                            resolved.setDefault(value);
                        } else {
                            resolved.setDefault((Boolean) null);
                        }
                    }
                }

                return property;
            }
        },
        BYTE_ARRAY(ByteArraySchema.class) {
            @Override
            protected boolean isType(String type, String format) {
                return ("string".equals(type) && "byte".equals(format));
            }

            @Override
            protected ByteArraySchema create() {
                return new ByteArraySchema();
            }
        },
        DATE(DateSchema.class) {
            @Override
            protected boolean isType(String type, String format) {
                return ("string".equals(type) && "date".equals(format));
            }

            @Override
            protected DateSchema create() {
                return new DateSchema();
            }
        },
        DATE_TIME(DateTimeSchema.class) {
            @Override
            protected boolean isType(String type, String format) {
                return ("string".equals(type) && "date-time".equals(format));
            }

            @Override
            protected DateTimeSchema create() {
                return new DateTimeSchema();
            }
        },
        INT(IntegerSchema.class) {
            @Override
            protected boolean isType(String type, String format) {
                return "integer".equals(type) && "int32".equals(format);
            }

            @Override
            protected IntegerSchema create() {
                return new IntegerSchema();
            }

            @Override
            public Schema merge(Schema property, Map<PropertyId, Object> args) {
                super.merge(property, args);
                if (property instanceof IntegerSchema) {
                    final IntegerSchema resolved = (IntegerSchema) property;
                    mergeNumeric(resolved, args);
                    if (args.containsKey(PropertyId.DEFAULT)) {
                        final String value = PropertyId.DEFAULT.findValue(args);
                        if (value != null) {
                            resolved.setDefault(value);
                        } else {
                            resolved.setDefault((Integer) null);
                        }
                    }
                }
                return property;
            }
        },
        LONG(IntegerSchema.class) {
            @Override
            protected boolean isType(String type, String format) {
                return "integer".equals(type) && "int64".equals(format);
            }

            @Override
            protected IntegerSchema create() {
                return new IntegerSchema().format("int64");
            }

            @Override
            public Schema merge(Schema property, Map<PropertyId, Object> args) {
                super.merge(property, args);
                if (property instanceof IntegerSchema) {
                    final IntegerSchema resolved = (IntegerSchema) property;
                    mergeNumeric(resolved, args);
                    if (args.containsKey(PropertyId.DEFAULT)) {
                        final String value = PropertyId.DEFAULT.findValue(args);
                        if (value != null) {
                            resolved.setDefault(value);
                        } else {
                            resolved.setDefault((Integer) null);
                        }
                    }
                }
                return property;
            }
        },

        // note: this must be in the enum order after both INT and LONG
        // (and any integer types added in the future), so the more specific
        // ones will be found first.
        INTEGER(NumberSchema.class) {
            @Override
            protected boolean isType(String type, String format) {
                return "number".equals(type);
            }

            @Override
            protected NumberSchema create() {
                return new NumberSchema();
            }

            @Override
            public Schema merge(Schema property, Map<PropertyId, Object> args) {
                super.merge(property, args);
                if (property instanceof NumberSchema) {
                    final NumberSchema resolved = (NumberSchema) property;
                    mergeNumeric(resolved, args);
                }
                return property;
            }
        },
        FILE(FileSchema.class) {
            @Override
            protected boolean isType(String type, String format) {
                return "string".equals(type) && "binary".equals(format);
            }

            @Override
            protected FileSchema create() {
                return new FileSchema();
            }
        },
        REFERENCE(Schema.class) {
            @Override
            protected boolean isType(String type, String format) {
                return "$ref".equals(type);
            }

            @Override
            protected Schema create() {
                return new Schema();
            }

            @Override
            public Schema toModel(Schema property) {
                final Schema model = createRef(property.get$ref());
                model.setDescription(property.getDescription());
                return model;
            }
        },
        E_MAIL(EmailSchema.class) {
            @Override
            protected boolean isType(String type, String format) {
                return "string".equals(type) && "email".equals(format);
            }

            @Override
            protected EmailSchema create() {
                return new EmailSchema();
            }

            @Override
            public Schema merge(Schema property, Map<PropertyId, Object> args) {
                super.merge(property, args);
                if (property instanceof EmailSchema) {
                    mergeString((EmailSchema) property, args);
                }
                return property;
            }
        },
        UUID(UUIDSchema.class) {
            @Override
            protected boolean isType(String type, String format) {
                return ("string".equals(type) && "uuid".equals(format));
            }

            @Override
            protected UUIDSchema create() {
                return new UUIDSchema();
            }

            @Override
            public Schema merge(Schema property, Map<PropertyId, Object> args) {
                super.merge(property, args);
                if (property instanceof UUIDSchema) {
                    final UUIDSchema resolved = (UUIDSchema) property;
                    if (args.containsKey(PropertyId.DEFAULT)) {
                        final String value = PropertyId.DEFAULT.findValue(args);
                        property.setDefault(value);
                    }
                    if (args.containsKey(PropertyId.MIN_LENGTH)) {
                        final Integer value = PropertyId.MIN_LENGTH.findValue(args);
                        resolved.setMinLength(value);
                    }
                    if (args.containsKey(PropertyId.MAX_LENGTH)) {
                        final Integer value = PropertyId.MAX_LENGTH.findValue(args);
                        resolved.setMaxLength(value);
                    }
                    if (args.containsKey(PropertyId.PATTERN)) {
                        final String value = PropertyId.PATTERN.findValue(args);
                        resolved.setPattern(value);
                    }
                }
                return property;
            }
        },
        OBJECT(ObjectSchema.class) {
            @Override
            protected boolean isType(String type, String format) {
                return "object".equals(type);
            }

            @Override
            protected ObjectSchema create() {
                return new ObjectSchema();
            }
        },
        ARRAY(ArraySchema.class) {
            @Override
            protected boolean isType(String type, String format) {
                return "array".equals(type);
            }

            @Override
            protected ArraySchema create() {
                return new ArraySchema();
            }

            @Override
            public Schema merge(final Schema property, final Map<PropertyId, Object> args) {
                super.merge(property, args);
                if (property instanceof ArraySchema) {
                    final ArraySchema resolved = (ArraySchema) property;
                    if (args.containsKey(PropertyId.MIN_ITEMS)) {
                        final Integer value = PropertyId.MIN_ITEMS.findValue(args);
                        resolved.setMinItems(value);
                    }
                    if (args.containsKey(PropertyId.MAX_ITEMS)) {
                        final Integer value = PropertyId.MAX_ITEMS.findValue(args);
                        resolved.setMaxItems(value);
                    }
                }

                return property;
            }
        },
        MAP(MapSchema.class) {
            @Override
            protected boolean isType(String type, String format) {
                // Note: It's impossible to distinct MAP and OBJECT as they use the same
                // set of values for type and format
                return "object".equals(type);
            }

            @Override
            protected MapSchema create() {
                return new MapSchema();
            }
        },

        // String is intentionally last, so it is found after the more specific property
        // types which also use the "string" type.
        STRING(StringSchema.class) {
            @Override
            protected boolean isType(final String type, final String format) {
                return "string".equals(type);
            }

            @Override
            protected StringSchema create() {
                return new StringSchema();
            }

            @Override
            public Schema merge(final Schema property, final Map<PropertyId, Object> args) {
                super.merge(property, args);
                if (property instanceof StringSchema) {
                    mergeString((StringSchema) property, args);
                }

                return property;
            }

        },
        ;

        private final Class<? extends Schema> type;

        Processor(Class<? extends Schema> type) {
            this.type = type;
        }

        public static Processor fromType(String type, String format) {
            for (Processor item : values()) {
                if (item.isType(type, format)) {
                    return item;
                }
            }
            log.debug("no property for " + type + ", " + format);
            return null;
        }

        public static Processor fromProperty(Schema property) {
            for (Processor item : values()) {
                if (item.isType(property)) {
                    return item;
                }
            }
            log.error("no property for " + (property == null ? "null" : property.getClass().getName()));
            return null;
        }

        protected abstract boolean isType(String type, String format);

        protected boolean isType(Schema property) {
            return type.isInstance(property);
        }

        protected abstract Schema create();

        /**
         * Running a BigDecimal through unit tests yields incorrect results when the
         * generated value comes from a string and the final value is an integer.
         *
         * @param number
         * @return
         */
        private BigDecimal toBigDecimal(String number) {
            if (number.contains(".")) {
                return BigDecimal.valueOf(Double.parseDouble(number));
            } else {
                return BigDecimal.valueOf(Integer.parseInt(number));
            }
        }

        protected <N extends Schema<? extends Number>> N mergeNumeric(N property, Map<PropertyId, Object> args) {
            if (args.containsKey(PropertyId.MINIMUM) && args.get(PropertyId.MINIMUM) != null) {
                property.setMinimum(toBigDecimal(PropertyId.MINIMUM.findValue(args)));
            }
            if (args.containsKey(PropertyId.MAXIMUM) && args.get(PropertyId.MAXIMUM) != null) {
                property.setMaximum(toBigDecimal(PropertyId.MAXIMUM.findValue(args)));
            }
            if (args.containsKey(PropertyId.EXCLUSIVE_MINIMUM)) {
                final Boolean value = PropertyId.EXCLUSIVE_MINIMUM.findValue(args);
                property.setExclusiveMinimum(value);
            }
            if (args.containsKey(PropertyId.EXCLUSIVE_MAXIMUM)) {
                final Boolean value = PropertyId.EXCLUSIVE_MAXIMUM.findValue(args);
                property.setExclusiveMaximum(value);
            }
            if (args.containsKey(PropertyId.DEFAULT) && args.get(PropertyId.DEFAULT) != null) {
                property.setDefault(toBigDecimal(PropertyId.DEFAULT.findValue(args)));
            }
            return property;
        }

        protected <N extends Schema<String>> N mergeString(N property, Map<PropertyId, Object> args) {
            if (args.containsKey(PropertyId.DEFAULT)) {
                final String value = PropertyId.DEFAULT.findValue(args);
                property.setDefault(value);
            }
            if (args.containsKey(PropertyId.MIN_LENGTH)) {
                final Integer value = PropertyId.MIN_LENGTH.findValue(args);
                property.setMinLength(value);
            }
            if (args.containsKey(PropertyId.MAX_LENGTH)) {
                final Integer value = PropertyId.MAX_LENGTH.findValue(args);
                property.setMaxLength(value);
            }
            if (args.containsKey(PropertyId.PATTERN)) {
                final String value = PropertyId.PATTERN.findValue(args);
                property.setPattern(value);
            }
            if (args.containsKey(PropertyId.ENUM)) {
                final List<String> value = PropertyId.ENUM.findValue(args);
                property.setEnum(value);
            }
            return property;
        }

        protected Schema createModel(Schema property) {
            return new Schema().type(property.getType()).format(property.getFormat())
                    .description(property.getDescription());
        }

        protected Schema createStringModel(StringSchema property) {
            final Schema model = createModel(property);
            model.setDefault(property.getDefault());
            return model;
        }

        /**
         * Creates new property on the passed arguments.
         *
         * @param args mapping of argument identifier to value
         * @return new property instance
         */
        public Schema build(Map<PropertyId, Object> args) {
            return merge(create(), args);
        }

        /**
         * Merges passed arguments into an existing property instance.
         *
         * @param property property to be updated
         * @param args     mapping of argument identifier to value. <code>null</code>s
         *                 will replace existing values
         * @return updated property instance
         */
        public Schema merge(Schema property, Map<PropertyId, Object> args) {
            if(args.containsKey(PropertyId.READ_ONLY)) {
                property.setReadOnly(PropertyId.READ_ONLY.<Boolean>findValue(args));
            }
            if (property.getFormat() == null) {
                property.setFormat(PropertyId.FORMAT.findValue(args));
            }
            if (args.containsKey(PropertyId.TITLE)) {
                final String value = PropertyId.TITLE.findValue(args);
                property.setTitle(value);
            }
            if (args.containsKey(PropertyId.DESCRIPTION)) {
                final String value = PropertyId.DESCRIPTION.findValue(args);
                property.setDescription(value);
            }
            if (args.containsKey(PropertyId.EXAMPLE)) {
                final String value = PropertyId.EXAMPLE.findValue(args);
                property.setExample(value);
            }
            // TODO :: find if vendor extensions have been replaced or removed
            /*if(args.containsKey(PropertyId.VENDOR_EXTENSIONS)) {
                final Map<String, Object> value = PropertyId.VENDOR_EXTENSIONS.findValue(args);
                property.setVendorExtensionMap(value);
            }*/
            if (args.containsKey(PropertyId.UNIQUE_ITEMS)) {
                final Boolean uniqueItems = PropertyId.UNIQUE_ITEMS.findValue(args);
                if (Boolean.TRUE.equals(uniqueItems)) {
                    property.uniqueItems(true);
                }
            }
            if(args.containsKey(PropertyId.ENUM)) {
                final List<String> values = PropertyId.ENUM.findValue(args);
                if(values != null) {
                    if(property instanceof IntegerSchema) {
                        IntegerSchema p = (IntegerSchema) property;
                        for(String value : values) {
                            try {
                                p.addEnumItem(Integer.parseInt(value));
                            }
                            catch(Exception e) {
                                // continue
                            }
                        }
                    }
                    if(property instanceof NumberSchema) {
                        NumberSchema p = (NumberSchema) property;
                        for(String value : values) {
                            try {
                                p.addEnumItem(new BigDecimal(value));
                            }
                            catch(Exception e) {
                                // continue
                            }
                        }
                    }
                    if(property instanceof DateSchema) {
                        DateSchema p = (DateSchema) property;
                        for(String value : values) {
                            try {
                                p.addEnumItem(new SimpleDateFormat("dd-MM-yyyy").parse(value));
                            }
                            catch(Exception e) {
                                // continue
                            }
                        }
                    }
                    if(property instanceof DateTimeSchema) {
                        DateTimeSchema p = (DateTimeSchema) property;
                        for(String value : values) {
                            try {
                                p.addEnumItem(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(value));
                            }
                            catch(Exception e) {
                                // continue
                            }
                        }
                    }
                    if(property instanceof UUIDSchema) {
                        UUIDSchema p = (UUIDSchema) property;
                        for(String value : values) {
                            try {
                                p.addEnumItem(java.util.UUID.fromString(value));
                            }
                            catch(Exception e) {
                                // continue
                            }
                        }
                    }
                }
            }

            return property;
        }

        /**
         * Converts passed property into a model.
         *
         * @param property property to be converted
         * @return model instance or <code>null</code> for unknown types
         */
        public Schema toModel(Schema property) {
            return property;
        }
    }
}