package com.madstudio.madcalculator.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.madstudio.madcalculator.model.HistoryItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HistoryManager {

    private static final String PREFS_NAME = "mad_calculator_prefs";
    private static final String KEY_HISTORY = "history_json";
    private static final int MAX_HISTORY = 50;

    private static volatile HistoryManager instance;

    private final SharedPreferences prefs;
    private final List<HistoryItem> historyList = new ArrayList<>();

    private HistoryManager(Context context) {
        if (context != null) {
            this.prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            loadFromPreferences();
        } else {
            this.prefs = null;
        }
    }

    public static synchronized void init(Context context) {
        if (instance == null && context != null) {
            instance = new HistoryManager(context.getApplicationContext());
        }
    }

    public static synchronized HistoryManager getInstance(Context context) {
        if (instance == null) {
            instance = new HistoryManager(context != null ? context.getApplicationContext() : null);
        }
        return instance;
    }

    public static synchronized HistoryManager getInstance() {
        if (instance == null) {
            instance = new HistoryManager(null);
        }
        return instance;
    }

    public synchronized void add(String expression, String result) {
        if (expression == null || expression.trim().isEmpty() ||
                result == null || result.trim().isEmpty()) {
            return;
        }

        HistoryItem item = new HistoryItem(expression.trim(), result.trim(), System.currentTimeMillis());
        historyList.add(0, item);

        while (historyList.size() > MAX_HISTORY) {
            historyList.remove(historyList.size() - 1);
        }

        saveToPreferences();
    }

    public synchronized List<HistoryItem> getAll() {
        return new ArrayList<>(historyList);
    }

    public synchronized void clear() {
        historyList.clear();
        saveToPreferences();
    }

    public synchronized int size() {
        return historyList.size();
    }

    private synchronized void loadFromPreferences() {
        if (prefs == null) return;

        historyList.clear();
        String jsonString = prefs.getString(KEY_HISTORY, null);
        if (jsonString == null || jsonString.isEmpty()) {
            return;
        }

        try {
            JSONArray array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                if (obj != null) {
                    HistoryItem item = HistoryItem.fromJson(obj);
                    if (item != null) {
                        historyList.add(item);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private synchronized void saveToPreferences() {
        if (prefs == null) return;

        try {
            JSONArray array = new JSONArray();
            for (HistoryItem item : historyList) {
                array.put(item.toJson());
            }
            prefs.edit().putString(KEY_HISTORY, array.toString()).apply();
        } catch (Exception ignored) {
        }
    }
}
