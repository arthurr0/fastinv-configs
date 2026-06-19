package dev.privatefinal.config;

import eu.okaeri.configs.OkaeriConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class MenuConfig extends OkaeriConfig {

    private String title = "Menu";

    private int rows = 6;

    private List<String> pattern = new ArrayList<>();

    private LinkedHashMap<String, MenuItem> items = new LinkedHashMap<>();

    private List<Integer> contentSlots = new ArrayList<>();

    private String contentChar = null;

    private List<Integer> editableSlots = new ArrayList<>();

    private String editableChar = null;

    public String getTitle() {
        return this.title;
    }

    public int getRows() {
        return Math.min(6, Math.max(1, this.rows));
    }

    public int getSize() {
        return this.getRows() * 9;
    }

    public List<String> getPattern() {
        return this.pattern;
    }

    public LinkedHashMap<String, MenuItem> getItems() {
        return this.items;
    }

    public List<Integer> getContentSlots() {
        return this.contentSlots;
    }

    public String getContentChar() {
        return this.contentChar;
    }

    public List<Integer> getEditableSlots() {
        return this.editableSlots;
    }

    public String getEditableChar() {
        return this.editableChar;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setPattern(List<String> pattern) {
        this.pattern = pattern;
    }

    public void addItem(String key, MenuItem item) {
        this.items.put(key, item);
    }

    public void setContentSlots(List<Integer> contentSlots) {
        this.contentSlots = contentSlots;
    }

    public void setContentChar(String contentChar) {
        this.contentChar = contentChar;
    }

    public void setEditableSlots(List<Integer> editableSlots) {
        this.editableSlots = editableSlots;
    }

    public void setEditableChar(String editableChar) {
        this.editableChar = editableChar;
    }
}
