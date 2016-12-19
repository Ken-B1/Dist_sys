/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package sourcefiles;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public interface ServerProtocol {
  public static final org.apache.avro.Protocol PROTOCOL = org.apache.avro.Protocol.parse("{\"protocol\":\"ServerProtocol\",\"namespace\":\"sourcefiles\",\"types\":[{\"type\":\"record\",\"name\":\"TemperatureRecord\",\"fields\":[{\"name\":\"time\",\"type\":\"string\"},{\"name\":\"temperature\",\"type\":\"double\"}]}],\"messages\":{\"enter\":{\"request\":[{\"name\":\"type\",\"type\":\"string\"},{\"name\":\"ip\",\"type\":\"string\"}],\"response\":\"string\"},\"leave\":{\"request\":[{\"name\":\"userName\",\"type\":\"string\"}],\"response\":\"string\"},\"getClients\":{\"request\":[],\"response\":{\"type\":\"array\",\"items\":\"string\"}},\"getLightStatuses\":{\"request\":[],\"response\":{\"type\":\"array\",\"items\":\"string\"}},\"changeLightState\":{\"request\":[{\"name\":\"lightName\",\"type\":\"string\"}],\"response\":\"string\"},\"getFridgeInventory\":{\"request\":[{\"name\":\"fridgeName\",\"type\":\"string\"}],\"response\":{\"type\":\"array\",\"items\":\"string\"}},\"requestShowEmptyFridge\":{\"request\":[{\"name\":\"fridgeName\",\"type\":\"string\"}],\"response\":\"string\"},\"showCurrentHouseTemp\":{\"request\":[],\"response\":\"int\"},\"showTempHistory\":{\"request\":[],\"response\":{\"type\":\"map\",\"values\":\"int\"}},\"connectUserToFridge\":{\"request\":[{\"name\":\"fridgeName\",\"type\":\"string\"}],\"response\":\"string\"},\"showConnectedFridges\":{\"request\":[],\"response\":{\"type\":\"array\",\"items\":\"string\"}},\"updateTemperature\":{\"request\":[{\"name\":\"sensorName\",\"type\":\"string\"},{\"name\":\"TemperatureRecord\",\"type\":\"TemperatureRecord\"}],\"response\":\"null\"},\"notifyUsersOfEmptyFridge\":{\"request\":[{\"name\":\"fridgeName\",\"type\":\"string\"}],\"response\":\"string\"}}}");
  java.lang.CharSequence enter(java.lang.CharSequence type, java.lang.CharSequence ip) throws org.apache.avro.AvroRemoteException;
  java.lang.CharSequence leave(java.lang.CharSequence userName) throws org.apache.avro.AvroRemoteException;
  java.util.List<java.lang.CharSequence> getClients() throws org.apache.avro.AvroRemoteException;
  java.util.List<java.lang.CharSequence> getLightStatuses() throws org.apache.avro.AvroRemoteException;
  java.lang.CharSequence changeLightState(java.lang.CharSequence lightName) throws org.apache.avro.AvroRemoteException;
  java.util.List<java.lang.CharSequence> getFridgeInventory(java.lang.CharSequence fridgeName) throws org.apache.avro.AvroRemoteException;
  java.lang.CharSequence requestShowEmptyFridge(java.lang.CharSequence fridgeName) throws org.apache.avro.AvroRemoteException;
  int showCurrentHouseTemp() throws org.apache.avro.AvroRemoteException;
  java.util.Map<java.lang.CharSequence,java.lang.Integer> showTempHistory() throws org.apache.avro.AvroRemoteException;
  java.lang.CharSequence connectUserToFridge(java.lang.CharSequence fridgeName) throws org.apache.avro.AvroRemoteException;
  java.util.List<java.lang.CharSequence> showConnectedFridges() throws org.apache.avro.AvroRemoteException;
  java.lang.Void updateTemperature(java.lang.CharSequence sensorName, sourcefiles.TemperatureRecord TemperatureRecord) throws org.apache.avro.AvroRemoteException;
  java.lang.CharSequence notifyUsersOfEmptyFridge(java.lang.CharSequence fridgeName) throws org.apache.avro.AvroRemoteException;

  @SuppressWarnings("all")
  public interface Callback extends ServerProtocol {
    public static final org.apache.avro.Protocol PROTOCOL = sourcefiles.ServerProtocol.PROTOCOL;
    void enter(java.lang.CharSequence type, java.lang.CharSequence ip, org.apache.avro.ipc.Callback<java.lang.CharSequence> callback) throws java.io.IOException;
    void leave(java.lang.CharSequence userName, org.apache.avro.ipc.Callback<java.lang.CharSequence> callback) throws java.io.IOException;
    void getClients(org.apache.avro.ipc.Callback<java.util.List<java.lang.CharSequence>> callback) throws java.io.IOException;
    void getLightStatuses(org.apache.avro.ipc.Callback<java.util.List<java.lang.CharSequence>> callback) throws java.io.IOException;
    void changeLightState(java.lang.CharSequence lightName, org.apache.avro.ipc.Callback<java.lang.CharSequence> callback) throws java.io.IOException;
    void getFridgeInventory(java.lang.CharSequence fridgeName, org.apache.avro.ipc.Callback<java.util.List<java.lang.CharSequence>> callback) throws java.io.IOException;
    void requestShowEmptyFridge(java.lang.CharSequence fridgeName, org.apache.avro.ipc.Callback<java.lang.CharSequence> callback) throws java.io.IOException;
    void showCurrentHouseTemp(org.apache.avro.ipc.Callback<java.lang.Integer> callback) throws java.io.IOException;
    void showTempHistory(org.apache.avro.ipc.Callback<java.util.Map<java.lang.CharSequence,java.lang.Integer>> callback) throws java.io.IOException;
    void connectUserToFridge(java.lang.CharSequence fridgeName, org.apache.avro.ipc.Callback<java.lang.CharSequence> callback) throws java.io.IOException;
    void showConnectedFridges(org.apache.avro.ipc.Callback<java.util.List<java.lang.CharSequence>> callback) throws java.io.IOException;
    void updateTemperature(java.lang.CharSequence sensorName, sourcefiles.TemperatureRecord TemperatureRecord, org.apache.avro.ipc.Callback<java.lang.Void> callback) throws java.io.IOException;
    void notifyUsersOfEmptyFridge(java.lang.CharSequence fridgeName, org.apache.avro.ipc.Callback<java.lang.CharSequence> callback) throws java.io.IOException;
  }
}