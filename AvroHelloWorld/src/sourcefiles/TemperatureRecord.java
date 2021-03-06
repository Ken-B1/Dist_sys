/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package sourcefiles;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class TemperatureRecord extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"TemperatureRecord\",\"namespace\":\"sourcefiles\",\"fields\":[{\"name\":\"time\",\"type\":\"string\"},{\"name\":\"temperature\",\"type\":\"double\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public java.lang.CharSequence time;
  @Deprecated public double temperature;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public TemperatureRecord() {}

  /**
   * All-args constructor.
   */
  public TemperatureRecord(java.lang.CharSequence time, java.lang.Double temperature) {
    this.time = time;
    this.temperature = temperature;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return time;
    case 1: return temperature;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: time = (java.lang.CharSequence)value$; break;
    case 1: temperature = (java.lang.Double)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'time' field.
   */
  public java.lang.CharSequence getTime() {
    return time;
  }

  /**
   * Sets the value of the 'time' field.
   * @param value the value to set.
   */
  public void setTime(java.lang.CharSequence value) {
    this.time = value;
  }

  /**
   * Gets the value of the 'temperature' field.
   */
  public java.lang.Double getTemperature() {
    return temperature;
  }

  /**
   * Sets the value of the 'temperature' field.
   * @param value the value to set.
   */
  public void setTemperature(java.lang.Double value) {
    this.temperature = value;
  }

  /** Creates a new TemperatureRecord RecordBuilder */
  public static sourcefiles.TemperatureRecord.Builder newBuilder() {
    return new sourcefiles.TemperatureRecord.Builder();
  }
  
  /** Creates a new TemperatureRecord RecordBuilder by copying an existing Builder */
  public static sourcefiles.TemperatureRecord.Builder newBuilder(sourcefiles.TemperatureRecord.Builder other) {
    return new sourcefiles.TemperatureRecord.Builder(other);
  }
  
  /** Creates a new TemperatureRecord RecordBuilder by copying an existing TemperatureRecord instance */
  public static sourcefiles.TemperatureRecord.Builder newBuilder(sourcefiles.TemperatureRecord other) {
    return new sourcefiles.TemperatureRecord.Builder(other);
  }
  
  /**
   * RecordBuilder for TemperatureRecord instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<TemperatureRecord>
    implements org.apache.avro.data.RecordBuilder<TemperatureRecord> {

    private java.lang.CharSequence time;
    private double temperature;

    /** Creates a new Builder */
    private Builder() {
      super(sourcefiles.TemperatureRecord.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(sourcefiles.TemperatureRecord.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.time)) {
        this.time = data().deepCopy(fields()[0].schema(), other.time);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.temperature)) {
        this.temperature = data().deepCopy(fields()[1].schema(), other.temperature);
        fieldSetFlags()[1] = true;
      }
    }
    
    /** Creates a Builder by copying an existing TemperatureRecord instance */
    private Builder(sourcefiles.TemperatureRecord other) {
            super(sourcefiles.TemperatureRecord.SCHEMA$);
      if (isValidValue(fields()[0], other.time)) {
        this.time = data().deepCopy(fields()[0].schema(), other.time);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.temperature)) {
        this.temperature = data().deepCopy(fields()[1].schema(), other.temperature);
        fieldSetFlags()[1] = true;
      }
    }

    /** Gets the value of the 'time' field */
    public java.lang.CharSequence getTime() {
      return time;
    }
    
    /** Sets the value of the 'time' field */
    public sourcefiles.TemperatureRecord.Builder setTime(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.time = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'time' field has been set */
    public boolean hasTime() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'time' field */
    public sourcefiles.TemperatureRecord.Builder clearTime() {
      time = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'temperature' field */
    public java.lang.Double getTemperature() {
      return temperature;
    }
    
    /** Sets the value of the 'temperature' field */
    public sourcefiles.TemperatureRecord.Builder setTemperature(double value) {
      validate(fields()[1], value);
      this.temperature = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'temperature' field has been set */
    public boolean hasTemperature() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'temperature' field */
    public sourcefiles.TemperatureRecord.Builder clearTemperature() {
      fieldSetFlags()[1] = false;
      return this;
    }

    @Override
    public TemperatureRecord build() {
      try {
        TemperatureRecord record = new TemperatureRecord();
        record.time = fieldSetFlags()[0] ? this.time : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.temperature = fieldSetFlags()[1] ? this.temperature : (java.lang.Double) defaultValue(fields()[1]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
