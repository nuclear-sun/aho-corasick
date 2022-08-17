package org.sun.ahocorasick.zhtools;

public class PinyinInfo {

    private int code;

    private String text;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
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
                "code=" + code +
                ", text='" + text + '\'' +
                '}';
    }
}
