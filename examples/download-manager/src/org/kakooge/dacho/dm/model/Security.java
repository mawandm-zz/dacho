package org.kakooge.dacho.dm.model;

import java.util.Date;

public class Security{
    public String symbol;
    public Double high;
    public Date date;
    public Double close;
    public Double open;
    public Double low;
    public Double volume;
	@Override
	public String toString() {
		return "Security [symbol=" + symbol + ", high=" + high + ", date="
				+ date + ", close=" + close + ", open=" + open + ", low=" + low
				+ ", volume=" + volume + "]";
	}
}

