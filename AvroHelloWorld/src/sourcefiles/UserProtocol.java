/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package sourcefiles;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public interface UserProtocol {
  public static final org.apache.avro.Protocol PROTOCOL = org.apache.avro.Protocol.parse("{\"protocol\":\"UserProtocol\",\"namespace\":\"sourcefiles\",\"types\":[],\"messages\":{\"notifyOfEmptyFridge\":{\"request\":[{\"name\":\"fridgeName\",\"type\":\"string\"}],\"response\":\"string\"},\"notifyUsers\":{\"request\":[{\"name\":\"userName\",\"type\":\"string\"},{\"name\":\"state\",\"type\":\"string\"}],\"response\":\"null\"}}}");
  java.lang.CharSequence notifyOfEmptyFridge(java.lang.CharSequence fridgeName) throws org.apache.avro.AvroRemoteException;
  java.lang.Void notifyUsers(java.lang.CharSequence userName, java.lang.CharSequence state) throws org.apache.avro.AvroRemoteException;

  @SuppressWarnings("all")
  public interface Callback extends UserProtocol {
    public static final org.apache.avro.Protocol PROTOCOL = sourcefiles.UserProtocol.PROTOCOL;
    void notifyOfEmptyFridge(java.lang.CharSequence fridgeName, org.apache.avro.ipc.Callback<java.lang.CharSequence> callback) throws java.io.IOException;
    void notifyUsers(java.lang.CharSequence userName, java.lang.CharSequence state, org.apache.avro.ipc.Callback<java.lang.Void> callback) throws java.io.IOException;
  }
}