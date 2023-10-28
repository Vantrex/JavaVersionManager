package de.vantrex.jvm.util;

import java.util.Objects;

public class ProfileLine {

    private final int index;
    private String content;

    public ProfileLine(int index, String content) {
        this.index = index;
        this.content = content;
    }

    public int getIndex() {
        return index;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfileLine line = (ProfileLine) o;
        return index == line.index && Objects.equals(content, line.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, content);
    }
}