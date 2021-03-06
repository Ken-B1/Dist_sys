/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package sourcefiles;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public interface UserProtocol {
  public static final org.apache.avro.Protocol PROTOCOL = org.apache.avro.Protocol.parse("{\"protocol\":\"UserProtocol\",\"namespace\":\"sourcefiles\",\"types\":[{\"type\":\"record\",\"name\":\"TemperatureRecord\",\"fields\":[{\"name\":\"time\",\"type\":\"string\"},{\"name\":\"temperature\",\"type\":\"double\"}]},{\"type\":\"record\",\"name\":\"TemperatureAggregate\",\"fields\":[{\"name\":\"counter\",\"type\":\"int\"},{\"name\":\"record\",\"type\":\"TemperatureRecord\"}]},{\"type\":\"record\",\"name\":\"NeighbourData\",\"fields\":[{\"name\":\"ip\",\"type\":\"string\"},{\"name\":\"type\",\"type\":\"string\"}]}],\"messages\":{\"notifyOfEmptyFridge\":{\"request\":[{\"name\":\"fridgeName\",\"type\":\"string\"}],\"response\":\"string\"},\"notifyUsers\":{\"request\":[{\"name\":\"userName\",\"type\":\"string\"},{\"name\":\"state\",\"type\":\"string\"}],\"response\":\"null\"},\"enter\":{\"request\":[{\"name\":\"userName\",\"type\":\"string\"},{\"name\":\"ip\",\"type\":\"string\"},{\"name\":\"type\",\"type\":\"string\"}],\"response\":\"null\"},\"leave\":{\"request\":[{\"name\":\"userName\",\"type\":\"string\"},{\"name\":\"type\",\"type\":\"string\"}],\"response\":\"null\"},\"enterHouse\":{\"request\":[{\"name\":\"userName\",\"type\":\"string\"}],\"response\":\"null\"},\"leaveHouse\":{\"request\":[{\"name\":\"userName\",\"type\":\"string\"}],\"response\":\"null\"},\"updateTemperature\":{\"request\":[{\"name\":\"TemperatureAggregate\",\"type\":\"TemperatureAggregate\"}],\"response\":\"null\"},\"sendElectionMessage\":{\"request\":[{\"name\":\"previousId\",\"type\":\"string\"}],\"response\":\"null\"},\"sendElectedMessage\":{\"request\":[{\"name\":\"electedId\",\"type\":\"string\"},{\"name\":\"electedIp\",\"type\":\"string\"}],\"response\":\"null\"},\"getNeighbour\":{\"request\":[],\"response\":\"NeighbourData\"},\"clearNeighbour\":{\"request\":[],\"response\":\"null\"},\"addNeighbour\":{\"request\":[{\"name\":\"neighbourIp\",\"type\":\"string\"},{\"name\":\"neighbourType\",\"type\":\"string\"}],\"response\":\"string\"},\"updateRepDataNeighbours\":{\"request\":[{\"name\":\"neighbourList\",\"type\":{\"type\":\"map\",\"values\":\"NeighbourData\"}},{\"name\":\"lastNeighbourId\",\"type\":\"string\"}],\"response\":\"string\"},\"updateRepDataIdCounter\":{\"request\":[{\"name\":\"idCounter\",\"type\":\"int\"}],\"response\":\"string\"},\"grantFridgeAccess\":{\"request\":[{\"name\":\"fridgeIp\",\"type\":\"string\"},{\"name\":\"fridgeName\",\"type\":\"string\"}],\"response\":\"string\"},\"resetFridgeQueueStatus\":{\"request\":[],\"response\":\"null\"}}}");
  java.lang.CharSequence notifyOfEmptyFridge(java.lang.CharSequence fridgeName) throws org.apache.avro.AvroRemoteException;
  java.lang.Void notifyUsers(java.lang.CharSequence userName, java.lang.CharSequence state) throws org.apache.avro.AvroRemoteException;
  java.lang.Void enter(java.lang.CharSequence userName, java.lang.CharSequence ip, java.lang.CharSequence type) throws org.apache.avro.AvroRemoteException;
  java.lang.Void leave(java.lang.CharSequence userName, java.lang.CharSequence type) throws org.apache.avro.AvroRemoteException;
  java.lang.Void enterHouse(java.lang.CharSequence userName) throws org.apache.avro.AvroRemoteException;
  java.lang.Void leaveHouse(java.lang.CharSequence userName) throws org.apache.avro.AvroRemoteException;
  java.lang.Void updateTemperature(sourcefiles.TemperatureAggregate TemperatureAggregate) throws org.apache.avro.AvroRemoteException;
  java.lang.Void sendElectionMessage(java.lang.CharSequence previousId) throws org.apache.avro.AvroRemoteException;
  java.lang.Void sendElectedMessage(java.lang.CharSequence electedId, java.lang.CharSequence electedIp) throws org.apache.avro.AvroRemoteException;
  sourcefiles.NeighbourData getNeighbour() throws org.apache.avro.AvroRemoteException;
  java.lang.Void clearNeighbour() throws org.apache.avro.AvroRemoteException;
  java.lang.CharSequence addNeighbour(java.lang.CharSequence neighbourIp, java.lang.CharSequence neighbourType) throws org.apache.avro.AvroRemoteException;
  java.lang.CharSequence updateRepDataNeighbours(java.util.Map<java.lang.CharSequence,sourcefiles.NeighbourData> neighbourList, java.lang.CharSequence lastNeighbourId) throws org.apache.avro.AvroRemoteException;
  java.lang.CharSequence updateRepDataIdCounter(int idCounter) throws org.apache.avro.AvroRemoteException;
  java.lang.CharSequence grantFridgeAccess(java.lang.CharSequence fridgeIp, java.lang.CharSequence fridgeName) throws org.apache.avro.AvroRemoteException;
  java.lang.Void resetFridgeQueueStatus() throws org.apache.avro.AvroRemoteException;

  @SuppressWarnings("all")
  public interface Callback extends UserProtocol {
    public static final org.apache.avro.Protocol PROTOCOL = sourcefiles.UserProtocol.PROTOCOL;
    void notifyOfEmptyFridge(java.lang.CharSequence fridgeName, org.apache.avro.ipc.Callback<java.lang.CharSequence> callback) throws java.io.IOException;
    void notifyUsers(java.lang.CharSequence userName, java.lang.CharSequence state, org.apache.avro.ipc.Callback<java.lang.Void> callback) throws java.io.IOException;
    void enter(java.lang.CharSequence userName, java.lang.CharSequence ip, java.lang.CharSequence type, org.apache.avro.ipc.Callback<java.lang.Void> callback) throws java.io.IOException;
    void leave(java.lang.CharSequence userName, java.lang.CharSequence type, org.apache.avro.ipc.Callback<java.lang.Void> callback) throws java.io.IOException;
    void enterHouse(java.lang.CharSequence userName, org.apache.avro.ipc.Callback<java.lang.Void> callback) throws java.io.IOException;
    void leaveHouse(java.lang.CharSequence userName, org.apache.avro.ipc.Callback<java.lang.Void> callback) throws java.io.IOException;
    void updateTemperature(sourcefiles.TemperatureAggregate TemperatureAggregate, org.apache.avro.ipc.Callback<java.lang.Void> callback) throws java.io.IOException;
    void sendElectionMessage(java.lang.CharSequence previousId, org.apache.avro.ipc.Callback<java.lang.Void> callback) throws java.io.IOException;
    void sendElectedMessage(java.lang.CharSequence electedId, java.lang.CharSequence electedIp, org.apache.avro.ipc.Callback<java.lang.Void> callback) throws java.io.IOException;
    void getNeighbour(org.apache.avro.ipc.Callback<sourcefiles.NeighbourData> callback) throws java.io.IOException;
    void clearNeighbour(org.apache.avro.ipc.Callback<java.lang.Void> callback) throws java.io.IOException;
    void addNeighbour(java.lang.CharSequence neighbourIp, java.lang.CharSequence neighbourType, org.apache.avro.ipc.Callback<java.lang.CharSequence> callback) throws java.io.IOException;
    void updateRepDataNeighbours(java.util.Map<java.lang.CharSequence,sourcefiles.NeighbourData> neighbourList, java.lang.CharSequence lastNeighbourId, org.apache.avro.ipc.Callback<java.lang.CharSequence> callback) throws java.io.IOException;
    void updateRepDataIdCounter(int idCounter, org.apache.avro.ipc.Callback<java.lang.CharSequence> callback) throws java.io.IOException;
    void grantFridgeAccess(java.lang.CharSequence fridgeIp, java.lang.CharSequence fridgeName, org.apache.avro.ipc.Callback<java.lang.CharSequence> callback) throws java.io.IOException;
    void resetFridgeQueueStatus(org.apache.avro.ipc.Callback<java.lang.Void> callback) throws java.io.IOException;
  }
}