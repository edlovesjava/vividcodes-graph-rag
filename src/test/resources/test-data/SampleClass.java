package com.vividcodes.test;

import java.util.List;
import java.util.Map;

/**
 * Sample class for testing the Java parser
 */
public class SampleClass {
    
    private String name;
    private int count;
    private List<String> items;
    
    public SampleClass() {
        this.name = "default";
        this.count = 0;
    }
    
    public SampleClass(String name, int count) {
        this.name = name;
        this.count = count;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public List<String> getItems() {
        return items;
    }
    
    public void setItems(List<String> items) {
        this.items = items;
    }
    
    public void processItems() {
        if (items != null) {
            for (String item : items) {
                System.out.println("Processing: " + item);
            }
        }
    }
    
    public Map<String, Object> toMap() {
        return Map.of(
            "name", name,
            "count", count,
            "items", items
        );
    }
} 