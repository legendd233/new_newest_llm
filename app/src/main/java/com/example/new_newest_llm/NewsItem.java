package com.example.new_newest_llm;

public class NewsItem {
    private int id;
    private String title;
    private String summary;
    private String time;
    private boolean isFavorited;

    // 构造方法
    public NewsItem(int id, String title, String summary, String time, boolean isFavorited) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.time = time;
        this.isFavorited = isFavorited;
    }

    // Getter 和 Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isFavorited() {
        return isFavorited;
    }

    public void setFavorited(boolean favorited) {
        isFavorited = favorited;
    }
}
