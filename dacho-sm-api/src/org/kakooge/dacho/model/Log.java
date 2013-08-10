package org.kakooge.dacho.model;

import java.util.Date;

public class Log {
    private int serviceId;
    private Date time;
    private String message;
    private int Type;

    public Log(int serviceId, Date time, String message, int Type) {
        this.serviceId = serviceId;
        this.time = time;
        this.message = message;
        this.Type = Type;
    }

    public int getServiceId() {
        return serviceId;
    }

    public Date getTime() {
        return time;
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return Type;
    }
    
    public static void warn(String message){
        //Log log = new Log()
    }
    public static void error(String message){
        
    }
    public static void info(String message){
        
    }
}
