/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package sourcefiles;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class replicationrecord extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"replicationrecord\",\"namespace\":\"sourcefiles\",\"fields\":[{\"name\":\"x\",\"type\":{\"type\":\"map\",\"values\":\"string\"}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> x;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public replicationrecord() {}

  /**
   * All-args constructor.
   */
  public replicationrecord(java.util.Map<java.lang.CharSequence,java.lang.CharSequence> x) {
    this.x = x;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return x;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: x = (java.util.Map<java.lang.CharSequence,java.lang.CharSequence>)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'x' field.
   */
  public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> getX() {
    return x;
  }

  /**
   * Sets the value of the 'x' field.
   * @param value the value to set.
   */
  public void setX(java.util.Map<java.lang.CharSequence,java.lang.CharSequence> value) {
    this.x = value;
  }

  /** Creates a new replicationrecord RecordBuilder */
  public static sourcefiles.replicationrecord.Builder newBuilder() {
    return new sourcefiles.replicationrecord.Builder();
  }
  
  /** Creates a new replicationrecord RecordBuilder by copying an existing Builder */
  public static sourcefiles.replicationrecord.Builder newBuilder(sourcefiles.replicationrecord.Builder other) {
    return new sourcefiles.replicationrecord.Builder(other);
  }
  
  /** Creates a new replicationrecord RecordBuilder by copying an existing replicationrecord instance */
  public static sourcefiles.replicationrecord.Builder newBuilder(sourcefiles.replicationrecord other) {
    return new sourcefiles.replicationrecord.Builder(other);
  }
  
  /**
   * RecordBuilder for replicationrecord instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<replicationrecord>
    implements org.apache.avro.data.RecordBuilder<replicationrecord> {

    private java.util.Map<java.lang.CharSequence,java.lang.CharSequence> x;

    /** Creates a new Builder */
    private Builder() {
      super(sourcefiles.replicationrecord.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(sourcefiles.replicationrecord.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.x)) {
        this.x = data().deepCopy(fields()[0].schema(), other.x);
        fieldSetFlags()[0] = true;
      }
    }
    
    /** Creates a Builder by copying an existing replicationrecord instance */
    private Builder(sourcefiles.replicationrecord other) {
            super(sourcefiles.replicationrecord.SCHEMA$);
      if (isValidValue(fields()[0], other.x)) {
        this.x = data().deepCopy(fields()[0].schema(), other.x);
        fieldSetFlags()[0] = true;
      }
    }

    /** Gets the value of the 'x' field */
    public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> getX() {
      return x;
    }
    
    /** Sets the value of the 'x' field */
    public sourcefiles.replicationrecord.Builder setX(java.util.Map<java.lang.CharSequence,java.lang.CharSequence> value) {
      validate(fields()[0], value);
      this.x = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'x' field has been set */
    public boolean hasX() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'x' field */
    public sourcefiles.replicationrecord.Builder clearX() {
      x = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    @Override
    public replicationrecord build() {
      try {
        replicationrecord record = new replicationrecord();
        record.x = fieldSetFlags()[0] ? this.x : (java.util.Map<java.lang.CharSequence,java.lang.CharSequence>) defaultValue(fields()[0]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}