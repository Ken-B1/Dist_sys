/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package sourcefiles;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class ReplicationData extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"ReplicationData\",\"namespace\":\"sourcefiles\",\"fields\":[{\"name\":\"connectedUsers\",\"type\":{\"type\":\"map\",\"values\":\"string\"}},{\"name\":\"connectedLights\",\"type\":{\"type\":\"map\",\"values\":\"string\"}},{\"name\":\"connectedFridges\",\"type\":{\"type\":\"map\",\"values\":\"string\"}},{\"name\":\"connectedTS\",\"type\":{\"type\":\"map\",\"values\":\"string\"}},{\"name\":\"temperatures\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"TemperatureAggregate\",\"fields\":[{\"name\":\"counter\",\"type\":\"int\"},{\"name\":\"record\",\"type\":{\"type\":\"record\",\"name\":\"TemperatureRecord\",\"fields\":[{\"name\":\"time\",\"type\":\"string\"},{\"name\":\"temperature\",\"type\":\"double\"}]}}]}}},{\"name\":\"userlocation\",\"type\":{\"type\":\"map\",\"values\":\"boolean\"}},{\"name\":\"neighbourList\",\"type\":{\"type\":\"map\",\"values\":{\"type\":\"record\",\"name\":\"NeighbourData\",\"fields\":[{\"name\":\"ip\",\"type\":\"string\"},{\"name\":\"type\",\"type\":\"string\"}]}}},{\"name\":\"idCounter\",\"type\":\"int\"},{\"name\":\"lastNeighbourId\",\"type\":\"string\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> connectedUsers;
  @Deprecated public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> connectedLights;
  @Deprecated public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> connectedFridges;
  @Deprecated public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> connectedTS;
  @Deprecated public java.util.List<sourcefiles.TemperatureAggregate> temperatures;
  @Deprecated public java.util.Map<java.lang.CharSequence,java.lang.Boolean> userlocation;
  @Deprecated public java.util.Map<java.lang.CharSequence,sourcefiles.NeighbourData> neighbourList;
  @Deprecated public int idCounter;
  @Deprecated public java.lang.CharSequence lastNeighbourId;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public ReplicationData() {}

  /**
   * All-args constructor.
   */
  public ReplicationData(java.util.Map<java.lang.CharSequence,java.lang.CharSequence> connectedUsers, java.util.Map<java.lang.CharSequence,java.lang.CharSequence> connectedLights, java.util.Map<java.lang.CharSequence,java.lang.CharSequence> connectedFridges, java.util.Map<java.lang.CharSequence,java.lang.CharSequence> connectedTS, java.util.List<sourcefiles.TemperatureAggregate> temperatures, java.util.Map<java.lang.CharSequence,java.lang.Boolean> userlocation, java.util.Map<java.lang.CharSequence,sourcefiles.NeighbourData> neighbourList, java.lang.Integer idCounter, java.lang.CharSequence lastNeighbourId) {
    this.connectedUsers = connectedUsers;
    this.connectedLights = connectedLights;
    this.connectedFridges = connectedFridges;
    this.connectedTS = connectedTS;
    this.temperatures = temperatures;
    this.userlocation = userlocation;
    this.neighbourList = neighbourList;
    this.idCounter = idCounter;
    this.lastNeighbourId = lastNeighbourId;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return connectedUsers;
    case 1: return connectedLights;
    case 2: return connectedFridges;
    case 3: return connectedTS;
    case 4: return temperatures;
    case 5: return userlocation;
    case 6: return neighbourList;
    case 7: return idCounter;
    case 8: return lastNeighbourId;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: connectedUsers = (java.util.Map<java.lang.CharSequence,java.lang.CharSequence>)value$; break;
    case 1: connectedLights = (java.util.Map<java.lang.CharSequence,java.lang.CharSequence>)value$; break;
    case 2: connectedFridges = (java.util.Map<java.lang.CharSequence,java.lang.CharSequence>)value$; break;
    case 3: connectedTS = (java.util.Map<java.lang.CharSequence,java.lang.CharSequence>)value$; break;
    case 4: temperatures = (java.util.List<sourcefiles.TemperatureAggregate>)value$; break;
    case 5: userlocation = (java.util.Map<java.lang.CharSequence,java.lang.Boolean>)value$; break;
    case 6: neighbourList = (java.util.Map<java.lang.CharSequence,sourcefiles.NeighbourData>)value$; break;
    case 7: idCounter = (java.lang.Integer)value$; break;
    case 8: lastNeighbourId = (java.lang.CharSequence)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'connectedUsers' field.
   */
  public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> getConnectedUsers() {
    return connectedUsers;
  }

  /**
   * Sets the value of the 'connectedUsers' field.
   * @param value the value to set.
   */
  public void setConnectedUsers(java.util.Map<java.lang.CharSequence,java.lang.CharSequence> value) {
    this.connectedUsers = value;
  }

  /**
   * Gets the value of the 'connectedLights' field.
   */
  public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> getConnectedLights() {
    return connectedLights;
  }

  /**
   * Sets the value of the 'connectedLights' field.
   * @param value the value to set.
   */
  public void setConnectedLights(java.util.Map<java.lang.CharSequence,java.lang.CharSequence> value) {
    this.connectedLights = value;
  }

  /**
   * Gets the value of the 'connectedFridges' field.
   */
  public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> getConnectedFridges() {
    return connectedFridges;
  }

  /**
   * Sets the value of the 'connectedFridges' field.
   * @param value the value to set.
   */
  public void setConnectedFridges(java.util.Map<java.lang.CharSequence,java.lang.CharSequence> value) {
    this.connectedFridges = value;
  }

  /**
   * Gets the value of the 'connectedTS' field.
   */
  public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> getConnectedTS() {
    return connectedTS;
  }

  /**
   * Sets the value of the 'connectedTS' field.
   * @param value the value to set.
   */
  public void setConnectedTS(java.util.Map<java.lang.CharSequence,java.lang.CharSequence> value) {
    this.connectedTS = value;
  }

  /**
   * Gets the value of the 'temperatures' field.
   */
  public java.util.List<sourcefiles.TemperatureAggregate> getTemperatures() {
    return temperatures;
  }

  /**
   * Sets the value of the 'temperatures' field.
   * @param value the value to set.
   */
  public void setTemperatures(java.util.List<sourcefiles.TemperatureAggregate> value) {
    this.temperatures = value;
  }

  /**
   * Gets the value of the 'userlocation' field.
   */
  public java.util.Map<java.lang.CharSequence,java.lang.Boolean> getUserlocation() {
    return userlocation;
  }

  /**
   * Sets the value of the 'userlocation' field.
   * @param value the value to set.
   */
  public void setUserlocation(java.util.Map<java.lang.CharSequence,java.lang.Boolean> value) {
    this.userlocation = value;
  }

  /**
   * Gets the value of the 'neighbourList' field.
   */
  public java.util.Map<java.lang.CharSequence,sourcefiles.NeighbourData> getNeighbourList() {
    return neighbourList;
  }

  /**
   * Sets the value of the 'neighbourList' field.
   * @param value the value to set.
   */
  public void setNeighbourList(java.util.Map<java.lang.CharSequence,sourcefiles.NeighbourData> value) {
    this.neighbourList = value;
  }

  /**
   * Gets the value of the 'idCounter' field.
   */
  public java.lang.Integer getIdCounter() {
    return idCounter;
  }

  /**
   * Sets the value of the 'idCounter' field.
   * @param value the value to set.
   */
  public void setIdCounter(java.lang.Integer value) {
    this.idCounter = value;
  }

  /**
   * Gets the value of the 'lastNeighbourId' field.
   */
  public java.lang.CharSequence getLastNeighbourId() {
    return lastNeighbourId;
  }

  /**
   * Sets the value of the 'lastNeighbourId' field.
   * @param value the value to set.
   */
  public void setLastNeighbourId(java.lang.CharSequence value) {
    this.lastNeighbourId = value;
  }

  /** Creates a new ReplicationData RecordBuilder */
  public static sourcefiles.ReplicationData.Builder newBuilder() {
    return new sourcefiles.ReplicationData.Builder();
  }
  
  /** Creates a new ReplicationData RecordBuilder by copying an existing Builder */
  public static sourcefiles.ReplicationData.Builder newBuilder(sourcefiles.ReplicationData.Builder other) {
    return new sourcefiles.ReplicationData.Builder(other);
  }
  
  /** Creates a new ReplicationData RecordBuilder by copying an existing ReplicationData instance */
  public static sourcefiles.ReplicationData.Builder newBuilder(sourcefiles.ReplicationData other) {
    return new sourcefiles.ReplicationData.Builder(other);
  }
  
  /**
   * RecordBuilder for ReplicationData instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<ReplicationData>
    implements org.apache.avro.data.RecordBuilder<ReplicationData> {

    private java.util.Map<java.lang.CharSequence,java.lang.CharSequence> connectedUsers;
    private java.util.Map<java.lang.CharSequence,java.lang.CharSequence> connectedLights;
    private java.util.Map<java.lang.CharSequence,java.lang.CharSequence> connectedFridges;
    private java.util.Map<java.lang.CharSequence,java.lang.CharSequence> connectedTS;
    private java.util.List<sourcefiles.TemperatureAggregate> temperatures;
    private java.util.Map<java.lang.CharSequence,java.lang.Boolean> userlocation;
    private java.util.Map<java.lang.CharSequence,sourcefiles.NeighbourData> neighbourList;
    private int idCounter;
    private java.lang.CharSequence lastNeighbourId;

    /** Creates a new Builder */
    private Builder() {
      super(sourcefiles.ReplicationData.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(sourcefiles.ReplicationData.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.connectedUsers)) {
        this.connectedUsers = data().deepCopy(fields()[0].schema(), other.connectedUsers);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.connectedLights)) {
        this.connectedLights = data().deepCopy(fields()[1].schema(), other.connectedLights);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.connectedFridges)) {
        this.connectedFridges = data().deepCopy(fields()[2].schema(), other.connectedFridges);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.connectedTS)) {
        this.connectedTS = data().deepCopy(fields()[3].schema(), other.connectedTS);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.temperatures)) {
        this.temperatures = data().deepCopy(fields()[4].schema(), other.temperatures);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.userlocation)) {
        this.userlocation = data().deepCopy(fields()[5].schema(), other.userlocation);
        fieldSetFlags()[5] = true;
      }
      if (isValidValue(fields()[6], other.neighbourList)) {
        this.neighbourList = data().deepCopy(fields()[6].schema(), other.neighbourList);
        fieldSetFlags()[6] = true;
      }
      if (isValidValue(fields()[7], other.idCounter)) {
        this.idCounter = data().deepCopy(fields()[7].schema(), other.idCounter);
        fieldSetFlags()[7] = true;
      }
      if (isValidValue(fields()[8], other.lastNeighbourId)) {
        this.lastNeighbourId = data().deepCopy(fields()[8].schema(), other.lastNeighbourId);
        fieldSetFlags()[8] = true;
      }
    }
    
    /** Creates a Builder by copying an existing ReplicationData instance */
    private Builder(sourcefiles.ReplicationData other) {
            super(sourcefiles.ReplicationData.SCHEMA$);
      if (isValidValue(fields()[0], other.connectedUsers)) {
        this.connectedUsers = data().deepCopy(fields()[0].schema(), other.connectedUsers);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.connectedLights)) {
        this.connectedLights = data().deepCopy(fields()[1].schema(), other.connectedLights);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.connectedFridges)) {
        this.connectedFridges = data().deepCopy(fields()[2].schema(), other.connectedFridges);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.connectedTS)) {
        this.connectedTS = data().deepCopy(fields()[3].schema(), other.connectedTS);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.temperatures)) {
        this.temperatures = data().deepCopy(fields()[4].schema(), other.temperatures);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.userlocation)) {
        this.userlocation = data().deepCopy(fields()[5].schema(), other.userlocation);
        fieldSetFlags()[5] = true;
      }
      if (isValidValue(fields()[6], other.neighbourList)) {
        this.neighbourList = data().deepCopy(fields()[6].schema(), other.neighbourList);
        fieldSetFlags()[6] = true;
      }
      if (isValidValue(fields()[7], other.idCounter)) {
        this.idCounter = data().deepCopy(fields()[7].schema(), other.idCounter);
        fieldSetFlags()[7] = true;
      }
      if (isValidValue(fields()[8], other.lastNeighbourId)) {
        this.lastNeighbourId = data().deepCopy(fields()[8].schema(), other.lastNeighbourId);
        fieldSetFlags()[8] = true;
      }
    }

    /** Gets the value of the 'connectedUsers' field */
    public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> getConnectedUsers() {
      return connectedUsers;
    }
    
    /** Sets the value of the 'connectedUsers' field */
    public sourcefiles.ReplicationData.Builder setConnectedUsers(java.util.Map<java.lang.CharSequence,java.lang.CharSequence> value) {
      validate(fields()[0], value);
      this.connectedUsers = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'connectedUsers' field has been set */
    public boolean hasConnectedUsers() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'connectedUsers' field */
    public sourcefiles.ReplicationData.Builder clearConnectedUsers() {
      connectedUsers = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'connectedLights' field */
    public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> getConnectedLights() {
      return connectedLights;
    }
    
    /** Sets the value of the 'connectedLights' field */
    public sourcefiles.ReplicationData.Builder setConnectedLights(java.util.Map<java.lang.CharSequence,java.lang.CharSequence> value) {
      validate(fields()[1], value);
      this.connectedLights = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'connectedLights' field has been set */
    public boolean hasConnectedLights() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'connectedLights' field */
    public sourcefiles.ReplicationData.Builder clearConnectedLights() {
      connectedLights = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'connectedFridges' field */
    public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> getConnectedFridges() {
      return connectedFridges;
    }
    
    /** Sets the value of the 'connectedFridges' field */
    public sourcefiles.ReplicationData.Builder setConnectedFridges(java.util.Map<java.lang.CharSequence,java.lang.CharSequence> value) {
      validate(fields()[2], value);
      this.connectedFridges = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'connectedFridges' field has been set */
    public boolean hasConnectedFridges() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'connectedFridges' field */
    public sourcefiles.ReplicationData.Builder clearConnectedFridges() {
      connectedFridges = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /** Gets the value of the 'connectedTS' field */
    public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> getConnectedTS() {
      return connectedTS;
    }
    
    /** Sets the value of the 'connectedTS' field */
    public sourcefiles.ReplicationData.Builder setConnectedTS(java.util.Map<java.lang.CharSequence,java.lang.CharSequence> value) {
      validate(fields()[3], value);
      this.connectedTS = value;
      fieldSetFlags()[3] = true;
      return this; 
    }
    
    /** Checks whether the 'connectedTS' field has been set */
    public boolean hasConnectedTS() {
      return fieldSetFlags()[3];
    }
    
    /** Clears the value of the 'connectedTS' field */
    public sourcefiles.ReplicationData.Builder clearConnectedTS() {
      connectedTS = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    /** Gets the value of the 'temperatures' field */
    public java.util.List<sourcefiles.TemperatureAggregate> getTemperatures() {
      return temperatures;
    }
    
    /** Sets the value of the 'temperatures' field */
    public sourcefiles.ReplicationData.Builder setTemperatures(java.util.List<sourcefiles.TemperatureAggregate> value) {
      validate(fields()[4], value);
      this.temperatures = value;
      fieldSetFlags()[4] = true;
      return this; 
    }
    
    /** Checks whether the 'temperatures' field has been set */
    public boolean hasTemperatures() {
      return fieldSetFlags()[4];
    }
    
    /** Clears the value of the 'temperatures' field */
    public sourcefiles.ReplicationData.Builder clearTemperatures() {
      temperatures = null;
      fieldSetFlags()[4] = false;
      return this;
    }

    /** Gets the value of the 'userlocation' field */
    public java.util.Map<java.lang.CharSequence,java.lang.Boolean> getUserlocation() {
      return userlocation;
    }
    
    /** Sets the value of the 'userlocation' field */
    public sourcefiles.ReplicationData.Builder setUserlocation(java.util.Map<java.lang.CharSequence,java.lang.Boolean> value) {
      validate(fields()[5], value);
      this.userlocation = value;
      fieldSetFlags()[5] = true;
      return this; 
    }
    
    /** Checks whether the 'userlocation' field has been set */
    public boolean hasUserlocation() {
      return fieldSetFlags()[5];
    }
    
    /** Clears the value of the 'userlocation' field */
    public sourcefiles.ReplicationData.Builder clearUserlocation() {
      userlocation = null;
      fieldSetFlags()[5] = false;
      return this;
    }

    /** Gets the value of the 'neighbourList' field */
    public java.util.Map<java.lang.CharSequence,sourcefiles.NeighbourData> getNeighbourList() {
      return neighbourList;
    }
    
    /** Sets the value of the 'neighbourList' field */
    public sourcefiles.ReplicationData.Builder setNeighbourList(java.util.Map<java.lang.CharSequence,sourcefiles.NeighbourData> value) {
      validate(fields()[6], value);
      this.neighbourList = value;
      fieldSetFlags()[6] = true;
      return this; 
    }
    
    /** Checks whether the 'neighbourList' field has been set */
    public boolean hasNeighbourList() {
      return fieldSetFlags()[6];
    }
    
    /** Clears the value of the 'neighbourList' field */
    public sourcefiles.ReplicationData.Builder clearNeighbourList() {
      neighbourList = null;
      fieldSetFlags()[6] = false;
      return this;
    }

    /** Gets the value of the 'idCounter' field */
    public java.lang.Integer getIdCounter() {
      return idCounter;
    }
    
    /** Sets the value of the 'idCounter' field */
    public sourcefiles.ReplicationData.Builder setIdCounter(int value) {
      validate(fields()[7], value);
      this.idCounter = value;
      fieldSetFlags()[7] = true;
      return this; 
    }
    
    /** Checks whether the 'idCounter' field has been set */
    public boolean hasIdCounter() {
      return fieldSetFlags()[7];
    }
    
    /** Clears the value of the 'idCounter' field */
    public sourcefiles.ReplicationData.Builder clearIdCounter() {
      fieldSetFlags()[7] = false;
      return this;
    }

    /** Gets the value of the 'lastNeighbourId' field */
    public java.lang.CharSequence getLastNeighbourId() {
      return lastNeighbourId;
    }
    
    /** Sets the value of the 'lastNeighbourId' field */
    public sourcefiles.ReplicationData.Builder setLastNeighbourId(java.lang.CharSequence value) {
      validate(fields()[8], value);
      this.lastNeighbourId = value;
      fieldSetFlags()[8] = true;
      return this; 
    }
    
    /** Checks whether the 'lastNeighbourId' field has been set */
    public boolean hasLastNeighbourId() {
      return fieldSetFlags()[8];
    }
    
    /** Clears the value of the 'lastNeighbourId' field */
    public sourcefiles.ReplicationData.Builder clearLastNeighbourId() {
      lastNeighbourId = null;
      fieldSetFlags()[8] = false;
      return this;
    }

    @Override
    public ReplicationData build() {
      try {
        ReplicationData record = new ReplicationData();
        record.connectedUsers = fieldSetFlags()[0] ? this.connectedUsers : (java.util.Map<java.lang.CharSequence,java.lang.CharSequence>) defaultValue(fields()[0]);
        record.connectedLights = fieldSetFlags()[1] ? this.connectedLights : (java.util.Map<java.lang.CharSequence,java.lang.CharSequence>) defaultValue(fields()[1]);
        record.connectedFridges = fieldSetFlags()[2] ? this.connectedFridges : (java.util.Map<java.lang.CharSequence,java.lang.CharSequence>) defaultValue(fields()[2]);
        record.connectedTS = fieldSetFlags()[3] ? this.connectedTS : (java.util.Map<java.lang.CharSequence,java.lang.CharSequence>) defaultValue(fields()[3]);
        record.temperatures = fieldSetFlags()[4] ? this.temperatures : (java.util.List<sourcefiles.TemperatureAggregate>) defaultValue(fields()[4]);
        record.userlocation = fieldSetFlags()[5] ? this.userlocation : (java.util.Map<java.lang.CharSequence,java.lang.Boolean>) defaultValue(fields()[5]);
        record.neighbourList = fieldSetFlags()[6] ? this.neighbourList : (java.util.Map<java.lang.CharSequence,sourcefiles.NeighbourData>) defaultValue(fields()[6]);
        record.idCounter = fieldSetFlags()[7] ? this.idCounter : (java.lang.Integer) defaultValue(fields()[7]);
        record.lastNeighbourId = fieldSetFlags()[8] ? this.lastNeighbourId : (java.lang.CharSequence) defaultValue(fields()[8]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
