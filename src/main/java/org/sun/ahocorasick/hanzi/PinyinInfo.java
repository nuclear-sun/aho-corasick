package org.sun.ahocorasick.hanzi;

public class PinyinInfo {

    private int id;

    private String text;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "PinyinInfo{" +
                "id=" + id +
                ", text='" + text + '\'' +
                '}';
    }
}
