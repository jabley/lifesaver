package com.textuality.lifesaver;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

public class Columns {

    private String[] names;

    private enum Type {
        STRING, INT, LONG, FLOAT, DOUBLE
    }

    private Type[] types;
    private int[] columns = null;
    private String key1 = null, key2 = null;
    private int key1Index = -1, key2Index;

    public Columns(String[] names, Class<?>[] classes, String key1, String key2) {
        this.names = names;
        types = new Type[names.length];
        this.key1 = key1;
        this.key2 = key2;
        for (int i = 0; i < names.length; i++) {

            if (classes[i] == String.class)
                types[i] = Type.STRING;
            else if (classes[i] == Integer.TYPE || classes[i] == Integer.class)
                types[i] = Type.INT;
            else if (classes[i] == Long.TYPE || classes[i] == Long.class)
                types[i] = Type.LONG;
            else if (classes[i] == Float.TYPE || classes[i] == Float.class)
                types[i] = Type.FLOAT;
            else if (classes[i] == Double.TYPE || classes[i] == Double.class)
                types[i] = Type.DOUBLE;
        }
    }

    private void setColumns(Cursor cursor) {
        if (columns != null)
            return;
        columns = new int[names.length];
        for (int i = 0; i < names.length; i++)
            columns[i] = cursor.getColumnIndex(names[i]);
    }

    public String jsonToKey(JSONObject json) {
        return json.opt(key1).toString() + "/" + json.opt(key2).toString();
    }

    public String cursorToKey(Cursor c) {
        if (key1Index == -1) {
            key1Index = c.getColumnIndex(key1);
            key2Index = c.getColumnIndex(key2);
        }
        return c.getString(key1Index) + "/" + c.getString(key2Index);
    }

    public JSONObject cursorToJSON(Cursor cursor) {
        setColumns(cursor);
        JSONObject json = new JSONObject();
        try {
            for (int i = 0; i < names.length; i++) {
                int col = columns[i];
                if (cursor.isNull(col))
                    continue;
                switch (types[i]) {
                case STRING:
                    json.put(names[i], cursor.getString(col));
                    break;
                case INT:
                    json.put(names[i], cursor.getInt(col));
                    break;
                case LONG:
                    json.put(names[i], cursor.getLong(col));
                    break;
                case FLOAT:
                    json.put(names[i], cursor.getFloat(col));
                    break;
                case DOUBLE:
                    json.put(names[i], cursor.getDouble(col));
                    break;
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return json;
    }

    public ContentValues jsonToContentValues(JSONObject j) {
        ContentValues cv = new ContentValues();
        for (int i = 0; i < names.length; i++) {
            switch (types[i]) {
            case STRING:
                j2cvString(j, cv, names[i]);
                break;
            case INT:
                j2cvInt(j, cv, names[i]);
                break;
            case LONG:
                j2cvLong(j, cv, names[i]);
                break;
            case FLOAT:
                j2cvFloat(j, cv, names[i]);
                break;
            case DOUBLE:
                j2cvDouble(j, cv, names[i]);
                break;
            }
        }

        return cv;
    }

    private static void j2cvInt(JSONObject j, ContentValues cv, String key) {
        try {
            int v = j.getInt(key);
            cv.put(key, v);
        } catch (JSONException e) {
        }
    }

    private static void j2cvLong(JSONObject j, ContentValues cv, String key) {
        try {
            long v = j.getLong(key);
            cv.put(key, v);
        } catch (JSONException e) {
        }
    }

    private static void j2cvString(JSONObject j, ContentValues cv, String key) {
        try {
            String v = j.getString(key);
            cv.put(key, v);
        } catch (JSONException e) {
        }
    }

    private static void j2cvFloat(JSONObject j, ContentValues cv, String key) {
        try {
            float v = (float) j.getDouble(key);
            cv.put(key, v);
        } catch (JSONException e) {
        }
    }

    private static void j2cvDouble(JSONObject j, ContentValues cv, String key) {
        try {
            double v = j.getDouble(key);
            cv.put(key, v);
        } catch (JSONException e) {
        }
    }

}
