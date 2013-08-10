package org.kakooge.dacho.model;

public class Classpath {
    private int serviceId;
    private String filePath;

    public Classpath(int serviceId, String filePath) {
        this.serviceId = serviceId;
        this.filePath = filePath;
    }

    public int getServiceId() {
        return serviceId;
    }

    public String getFilePath() {
        return filePath;
    }
}
