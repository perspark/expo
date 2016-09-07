// Copyright 2015-present 650 Industries. All rights reserved.

package host.exp.exponent.utils;

import android.os.Bundle;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

import host.exp.exponent.analytics.EXL;

// From com.facebook.internal.BundleJSONConverter. BundleJSONConverter doesn't support
// JSONObjects inside of JSONArrays
public class JSONBundleConverter {

  private static final String TAG = JSONBundleConverter.class.getSimpleName();

  public static Bundle JSONToBundle(JSONObject jsonObject) {
    Bundle bundle = new Bundle();
    @SuppressWarnings("unchecked")
    Iterator<String> jsonIterator = jsonObject.keys();
    while (jsonIterator.hasNext()) {
      try {
        String key = jsonIterator.next();
        Object value = jsonObject.get(key);
        if (value == null || value == JSONObject.NULL) {
          // Null is not supported.
          continue;
        }

        // Special case JSONObject as it's one way, on the return it would be Bundle.
        if (value instanceof JSONObject) {
          bundle.putBundle(key, JSONToBundle((JSONObject) value));
          continue;
        }

        Class clazz = value.getClass();
        if (clazz == JSONArray.class) {
          setJSONArray(bundle, key, (JSONArray) value);
        } else if (clazz == String.class) {
          bundle.putString(key, (String) value);
        } else if (clazz == Boolean.class) {
          bundle.putBoolean(key, (Boolean) value);
        } else if (clazz == Integer.class) {
          bundle.putInt(key, (Integer) value);
        } else if (clazz == Long.class) {
          bundle.putLong(key, (Long) value);
        } else if (clazz == Double.class) {
          bundle.putDouble(key, (Double) value);
        } else if (clazz == String.class) {
          throw new IllegalArgumentException("Unexpected type: " + clazz);
        }
      } catch (JSONException e) {
        continue;
      }
    }

    return bundle;
  }

  private static Object convertToJavaArray(Class clazz, JSONArray jsonArray) throws JSONException {
    Object array = Array.newInstance(clazz, jsonArray.length());
    for (int i = 0; i < jsonArray.length(); i++) {
      Object current = jsonArray.get(i);
      if (clazz.isAssignableFrom(current.getClass())) {
        Array.set(array, i, current);
      } else {
        throw new IllegalArgumentException("Unexpected type in an array: " + current.getClass() + ". All array elements must be same type.");
      }
    }

    return array;
  }

  private static void setJSONArray(Bundle bundle, String key, JSONArray jsonArray) throws JSONException {
    // Empty list, can't even figure out the type, assume an ArrayList<String>
    if (jsonArray.length() == 0) {
      bundle.putStringArray(key, new String[0]);
    } else {
      Object first = jsonArray.get(0);

      // Blurghhhhh. We could do this all with reflection but this seems (slightly) better maybe?
      if (byte.class.isAssignableFrom(first.getClass())) {
        bundle.putByteArray(key, (byte[]) convertToJavaArray(byte.class, jsonArray));
      } else if (char.class.isAssignableFrom(first.getClass())) {
        bundle.putCharArray(key, (char[]) convertToJavaArray(char.class, jsonArray));
      } else if (float.class.isAssignableFrom(first.getClass())) {
        bundle.putFloatArray(key, (float[]) convertToJavaArray(float.class, jsonArray));
      } else if (short.class.isAssignableFrom(first.getClass())) {
        bundle.putShortArray(key, (short[]) convertToJavaArray(short.class, jsonArray));
      } else if (String.class.isAssignableFrom(first.getClass())) {
        bundle.putStringArray(key, (String[]) convertToJavaArray(String.class, jsonArray));
      } else if (boolean.class.isAssignableFrom(first.getClass())) {
        bundle.putBooleanArray(key, (boolean[]) convertToJavaArray(boolean.class, jsonArray));
      } else if (double.class.isAssignableFrom(first.getClass())) {
        bundle.putDoubleArray(key, (double[]) convertToJavaArray(double.class, jsonArray));
      } else if (int.class.isAssignableFrom(first.getClass())) {
        bundle.putIntArray(key, (int[]) convertToJavaArray(int.class, jsonArray));
      } else if (long.class.isAssignableFrom(first.getClass())) {
        bundle.putLongArray(key, (long[]) convertToJavaArray(long.class, jsonArray));
      } else if (first instanceof JSONObject) {
        ArrayList<Bundle> parcelableArrayList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
          Object current = jsonArray.get(i);
          if (current instanceof JSONObject) {
            parcelableArrayList.add(JSONToBundle((JSONObject) current));
          } else {
            throw new IllegalArgumentException("Unexpected type in an array: " + current.getClass());
          }
        }

        Bundle[] array = new Bundle[parcelableArrayList.size()];
        array = parcelableArrayList.toArray(array);
        bundle.putParcelableArray(key, array);
      } else {
        throw new IllegalArgumentException("Unexpected type in an array: " + first.getClass());
      }
    }
  }

  public static JSONObject readableMapToJson(ReadableMap map) {
    // TODO: maybe leverage Arguments.toBundle somehow?
    JSONObject json = new JSONObject();

    try {
      ReadableMapKeySetIterator iterator = map.keySetIterator();
      while (iterator.hasNextKey()) {
        String key = iterator.nextKey();
        switch (map.getType(key)) {
          case Null:
            json.put(key, null);
            break;
          case Boolean:
            json.put(key, map.getBoolean(key));
            break;
          case Number:
            json.put(key, map.getDouble(key));
            break;
          case String:
            json.put(key, map.getString(key));
            break;
          case Map:
            json.put(key, readableMapToJson(map.getMap(key)));
            break;
          case Array:
            json.put(key, readableArrayToJson(map.getArray(key)));
            break;
        }
      }
    } catch (JSONException e) {
      // TODO
      EXL.d(TAG, "Error converting ReadableMap to json: " + e.toString());
    }

    return json;
  }

  public static JSONArray readableArrayToJson(ReadableArray array) {
    // TODO: maybe leverage Arguments.toBundle somehow?
    JSONArray json = new JSONArray();

    try {
      for (int i = 0; i < array.size(); i++) {
        switch (array.getType(i)) {
          case Null:
            json.put(null);
            break;
          case Boolean:
            json.put(array.getBoolean(i));
            break;
          case Number:
            json.put(array.getDouble(i));
            break;
          case String:
            json.put(array.getString(i));
            break;
          case Map:
            json.put(readableMapToJson(array.getMap(i)));
            break;
          case Array:
            json.put(readableArrayToJson(array.getArray(i)));
            break;
        }
      }
    } catch (JSONException e) {
      // TODO
      EXL.d(TAG, "Error converting ReadableArray to json: " + e.toString());
    }

    return json;
  }
}
