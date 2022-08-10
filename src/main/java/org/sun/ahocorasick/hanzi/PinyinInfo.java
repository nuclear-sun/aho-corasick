package org.sun.ahocorasick.hanzi;

public class PinyinInfo {

    private int id;

    private String pinyin;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    @Override
    public String toString() {
        return "PinyinInfo{" +
                "id=" + id +
                ", pinyin='" + pinyin + '\'' +
                '}';
    }
}
