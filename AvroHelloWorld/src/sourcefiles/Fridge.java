/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package sourcefiles;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class Fridge extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Fridge\",\"namespace\":\"sourcefiles\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"content\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public java.lang.CharSequence name;
  @Deprecated public java.util.List<java.lang.CharSequence> content;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public Fridge() {}

  /**
   * All-args constructor.
   */
  public Fridge(java.lang.CharSequence name, java.util.List<java.lang.CharSequence> content) {
    this.name = name;
    this.content = content;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return name;
    case 1: return content;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: name = (java.lang.CharSequence)value$; break;
    case 1: content = (java.util.List<java.lang.CharSequence>)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'name' field.
   */
  public java.lang.CharSequence getName() {
    return name;
  }

  /**
   * Sets the value of the 'name' field.
   * @param value the value to set.
   */
  public void setName(java.lang.CharSequence value) {
    this.name = value;
  }

  /**
   * Gets the value of the 'content' field.
   */
  public java.util.List<java.lang.CharSequence> getContent() {
    return content;
  }

  /**
   * Sets the value of the 'content' field.
   * @param value the value to set.
   */
  public void setContent(java.util.List<java.lang.CharSequence> value) {
    this.content = value;
  }

  /** Creates a new Fridge RecordBuilder */
  public static sourcefiles.Fridge.Builder newBuilder() {
    return new sourcefiles.Fridge.Builder();
  }
  
  /** Creates a new Fridge RecordBuilder by copying an existing Builder */
  public static sourcefiles.Fridge.Builder newBuilder(sourcefiles.Fridge.Builder other) {
    return new sourcefiles.Fridge.Builder(other);
  }
  
  /** Creates a new Fridge RecordBuilder by copying an existing Fridge instance */
  public static sourcefiles.Fridge.Builder newBuilder(sourcefiles.Fridge other) {
    return new sourcefiles.Fridge.Builder(other);
  }
  
  /**
   * RecordBuilder for Fridge instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<Fridge>
    implements org.apache.avro.data.RecordBuilder<Fridge> {

    private java.lang.CharSequence name;
    private java.util.List<java.lang.CharSequence> content;

    /** Creates a new Builder */
    private Builder() {
      super(sourcefiles.Fridge.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(sourcefiles.Fridge.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.name)) {
        this.name = data().deepCopy(fields()[0].schema(), other.name);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.content)) {
        this.content = data().deepCopy(fields()[1].schema(), other.content);
        fieldSetFlags()[1] = true;
      }
    }
    
    /** Creates a Builder by copying an existing Fridge instance */
    private Builder(sourcefiles.Fridge other) {
            super(sourcefiles.Fridge.SCHEMA$);
      if (isValidValue(fields()[0], other.name)) {
        this.name = data().deepCopy(fields()[0].schema(), other.name);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.content)) {
        this.content = data().deepCopy(fields()[1].schema(), other.content);
        fieldSetFlags()[1] = true;
      }
    }

    /** Gets the value of the 'name' field */
    public java.lang.CharSequence getName() {
      return name;
    }
    
    /** Sets the value of the 'name' field */
    public sourcefiles.Fridge.Builder setName(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.name = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'name' field has been set */
    public boolean hasName() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'name' field */
    public sourcefiles.Fridge.Builder clearName() {
      name = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'content' field */
    public java.util.List<java.lang.CharSequence> getContent() {
      return content;
    }
    
    /** Sets the value of the 'content' field */
    public sourcefiles.Fridge.Builder setContent(java.util.List<java.lang.CharSequence> value) {
      validate(fields()[1], value);
      this.content = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'content' field has been set */
    public boolean hasContent() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'content' field */
    public sourcefiles.Fridge.Builder clearContent() {
      content = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    @Override
    public Fridge build() {
      try {
        Fridge record = new Fridge();
        record.name = fieldSetFlags()[0] ? this.name : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.content = fieldSetFlags()[1] ? this.content : (java.util.List<java.lang.CharSequence>) defaultValue(fields()[1]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}