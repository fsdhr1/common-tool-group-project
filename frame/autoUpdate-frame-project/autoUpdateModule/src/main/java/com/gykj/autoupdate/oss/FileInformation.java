package com.gykj.autoupdate.oss;

public class FileInformation {
    private String name;//上传到阿里云的文件名
    private String path;//Android本地文件路径

    public FileInformation(String name, String path) {
        this.name = name;
        this.path = path;
    }
    public FileInformation(String path) {
        this.path = path;
        if (path != null) {
            String[] s = path.split("/");
            if (s.length > 0) {
                this.name = s[s.length - 1];
            }
        }
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
