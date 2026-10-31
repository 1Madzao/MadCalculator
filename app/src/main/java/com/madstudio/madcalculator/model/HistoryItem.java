package com.madstudio.madcalculator.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class HistoryItem {

    private String expression;
    private String result;
    private long timestamp;

    public HistoryItem() {
        this("", "", System.currentTimeMillis());
    }

    public HistoryItem(String expression, String result) {
        this(expression, result, System.currentTimeMillis());
    }

    public HistoryItem(String expression, String result, long timestamp) {
        this.expression = expression != null ? expression : "";
        this.result = result != null ? result : "";
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression != null ? expression : "";
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result != null ? result : "";
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedTime() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            return "";
        }
    }

    public String getFormattedDate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            return "";
        }
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("expression", expression);
        json.put("result", result);
        json.put("timestamp", timestamp);
        return json;
    }

    public static HistoryItem fromJson(JSONObject json) {
        if (json == null) return null;
        String expr = json.optString("expression", "");
        String res = json.optString("result", "");
        long time = json.optLong("timestamp", System.currentTimeMillis());
        return new HistoryItem(expr, res, time);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoryItem that = (HistoryItem) o;
        return timestamp == that.timestamp &&
                Objects.equals(expression, that.expression) &&
                Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression, result, timestamp);
    }

    @Override
    public String toString() {
        return "HistoryItem{" +
                "expression='" + expression + '\'' +
                ", result='" + result + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
