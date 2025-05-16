// package se.basile.compax;
//
// import android.util.Log;
// import com.google.gson.Gson;
// import com.google.gson.JsonArray;
// import com.google.gson.JsonElement;
// import com.google.gson.JsonObject;
// import com.google.gson.JsonPrimitive;
// import org.java_websocket.client.WebSocketClient;
// import org.java_websocket.handshake.ServerHandshake;
// import java.net.URI;
// import java.net.URISyntaxException;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.concurrent.Executors;
// import java.util.concurrent.ScheduledExecutorService;
// import java.util.concurrent.TimeUnit;
//
// public class SignalKClient {
//     private static final String TAG = "SignalKClient";
//     private static final int RECONNECT_INTERVAL = 5000;
//
//     private WebSocketClient wsClient;
//     private final Gson gson = new Gson();
//     private SignalKListener listener;
//     private final Map<String, Object> valueCache = new HashMap<>();
//     private ScheduledExecutorService reconnectExecutor;
//     private String serverUrl;
//
//     public interface SignalKListener {
//         void onConnected();
//         void onDisconnected();
//         void onValueUpdate(String path, Object value);
//         void onError(String message);
//     }
//
//     public SignalKClient(SignalKListener listener) {
//         this.listener = listener;
//     }
//
//     public void connect(String serverUrl) {
//         this.serverUrl = serverUrl;
//         disconnect();
//
//         try {
//             URI uri = new URI(serverUrl);
//             wsClient = new WebSocketClient(uri) {
//                 @Override
//                 public void onOpen(ServerHandshake handshakedata) {
//                     Log.i(TAG, "Connected to Signal K server");
//                     valueCache.clear();
//                     listener.onConnected();
//                     sendSubscriptionRequest();
//                 }
//
//                 @Override
//                 public void onMessage(String message) {
//                     processDeltaMessage(message);
//                 }
//
//                 @Override
//                 public void onClose(int code, String reason, boolean remote) {
//                     Log.w(TAG, "Connection closed: " + reason);
//                     listener.onDisconnected();
//                     scheduleReconnect();
//                 }
//
//                 @Override
//                 public void onError(Exception ex) {
//                     Log.e(TAG, "WebSocket error", ex);
//                     listener.onError(ex.getMessage());
//                 }
//             };
//
//             wsClient.connect();
//
//         } catch (URISyntaxException e) {
//             listener.onError("Invalid server URL: " + e.getMessage());
//         }
//     }
//
//     private void sendSubscriptionRequest() {
//         JsonObject subscription = new JsonObject();
//         JsonArray subscribe = new JsonArray();
//
//         String[] paths = {
//                 "navigation.position",
//                 "navigation.headingMagnetic",
//                 "navigation.speedOverGround",
//                 "environment.wind.speedApparent",
//                 "environment.wind.angleApparent",
//                 "steering.autopilot.state",
//                 "steering.autopilot.target.heading"
//         };
//
//         for (String path : paths) {
//             JsonObject sub = new JsonObject();
//             sub.addProperty("path", path);
//             sub.addProperty("period", 1000);
//             sub.addProperty("format", "delta");
//             subscribe.add(sub);
//         }
//
//         subscription.add("subscribe", subscribe);
//         wsClient.send(subscription.toString());
//     }
//
//     private void processDeltaMessage(String json) {
//         try {
//             JsonObject delta = gson.fromJson(json, JsonObject.class);
//             JsonArray updates = delta.getAsJsonArray("updates");
//
//             for (JsonElement updateElem : updates) {
//                 JsonObject update = updateElem.getAsJsonObject();
//                 JsonArray values = update.getAsJsonArray("values");
//
//                 for (JsonElement valueElem : values) {
//                     JsonObject valueObj = valueElem.getAsJsonObject();
//                     String path = valueObj.get("path").getAsString();
//                     JsonElement value = valueObj.get("value");
//
//                     Object parsedValue = parseJsonValue(value);
//                     valueCache.put(path, parsedValue);
//                     listener.onValueUpdate(path, parsedValue);
//                 }
//             }
//         } catch (Exception e) {
//             Log.e(TAG, "Error processing delta message", e);
//         }
//     }
//
//     private Object parseJsonValue(JsonElement value) {
//         if (value instanceof JsonPrimitive) {
//             JsonPrimitive primitive = (JsonPrimitive) value;
//             if (primitive.isNumber()) {
//                 return primitive.getAsDouble();
//             } else if (primitive.isBoolean()) {
//                 return primitive.getAsBoolean();
//             }
//             return primitive.getAsString();
//         }
//         return null;
//     }
//
//     public void sendCommand(String path, Object value) {
//         if (wsClient != null && wsClient.isOpen()) {
//             JsonObject delta = new JsonObject();
//             JsonArray updates = new JsonArray();
//             JsonObject update = new JsonObject();
//             JsonArray values = new JsonArray();
//             JsonObject valueObj = new JsonObject();
//
//             valueObj.addProperty("path", path);
//             if (value instanceof Number) {
//                 valueObj.addProperty("value", (Number) value);
//             } else if (value instanceof Boolean) {
//                 valueObj.addProperty("value", (Boolean) value);
//             } else {
//                 valueObj.addProperty("value", value.toString());
//             }
//
//             values.add(valueObj);
//             update.add("values", values);
//             updates.add(update);
//             delta.add("updates", updates);
//
//             wsClient.send(delta.toString());
//         }
//     }
//
//     private void scheduleReconnect() {
//         if (reconnectExecutor == null) {
//             reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
//             reconnectExecutor.scheduleAtFixedRate(() -> {
//                 if (!isConnected()) {
//                     Log.i(TAG, "Attempting reconnect...");
//                     connect(serverUrl);
//                 }
//             }, RECONNECT_INTERVAL, RECONNECT_INTERVAL, TimeUnit.MILLISECONDS);
//         }
//     }
//
//     public boolean isConnected() {
//         return wsClient != null && wsClient.isOpen();
//     }
//
//     public void disconnect() {
//         if (wsClient != null) {
//             wsClient.close();
//         }
//         if (reconnectExecutor != null) {
//             reconnectExecutor.shutdownNow();
//             reconnectExecutor = null;
//         }
//     }
//
//     public Object getCachedValue(String path) {
//         return valueCache.get(path);
//     }
// }