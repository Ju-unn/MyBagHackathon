package com.example.mybaghackathon.data.remote.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/** PHP/MySQL 응답의 true/false와 1/0을 모두 boolean으로 변환한다. */
public class FlexibleBooleanAdapter extends TypeAdapter<Boolean> {

    @Override
    public void write(JsonWriter out, Boolean value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value);
        }
    }

    @Override
    public Boolean read(JsonReader in) throws IOException {
        JsonToken token = in.peek();
        switch (token) {
            case BOOLEAN:
                return in.nextBoolean();
            case NUMBER:
                return in.nextDouble() != 0d;
            case STRING:
                String value = in.nextString().trim();
                return "1".equals(value) || "true".equalsIgnoreCase(value);
            case NULL:
                in.nextNull();
                return false;
            default:
                in.skipValue();
                return false;
        }
    }
}
