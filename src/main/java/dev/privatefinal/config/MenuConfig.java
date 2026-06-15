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

    private Character contentChar = null;

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

    public Character getContentChar() {
        return this.contentChar;
    }
}
