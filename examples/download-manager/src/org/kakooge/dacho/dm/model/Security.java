package org.kakooge.dacho.dm.model;

public class Security{
    public long symbol;
    public double high;
    public long date;
    public double close;
    public double open;
    public double low;
    public double volume;
    private long pad;
    
	@Override
	public String toString() {
		return "Security [symbol=" + symbol + ", high=" + high + ", date="
				+ date + ", close=" + close + ", open=" + open + ", low=" + low
				+ ", volume=" + volume + "]";
	}
	
	public static String getSymbolString(Security security){
		if(security.symbol > 0xFFFFFFFFFFL)
			throw new IllegalArgumentException("Invalid security code");
		byte[] symbolChars = new byte[5];
		
		symbolChars[0] = (byte)((security.symbol >> 32) & 0xFF);
		symbolChars[1] = (byte)((security.symbol >> 24) & 0xFF);
		symbolChars[2] = (byte)((security.symbol >> 16) & 0xFF);
		symbolChars[3] = (byte)((security.symbol >> 8) & 0xFF);
		symbolChars[4] = (byte)(security.symbol & 0xFF);
		
		return new String(symbolChars);
	}
	
	public static long getSymbolLong(String symbol){
		long code = 0;
		byte[] symbolChars = symbol.getBytes();
		
		switch(symbolChars.length){
		
		case 5:
			code = (long)symbolChars[4];
		case 4:
			code |= (long)symbolChars[3] << 8;	
		case 3:
			code |= (long)symbolChars[2] << 16;
		case 2:
			code |= (long)symbolChars[1] << 24;		
		case 1:
			code |= (long)symbolChars[0] << 32;
			break;
		default:
			throw new IllegalArgumentException("Invalid symbol" + symbol);
		}
		
		return code;
	}
	
	public static void main(String[] args){
		Security security = new Security();
		security.symbol = getSymbolLong("TPT.L");
		String symbol = Security.getSymbolString(security);
		System.out.println(symbol);
		
		security.symbol = getSymbolLong("TP.L");
		symbol = Security.getSymbolString(security);
		System.out.println(symbol);		
		
		security.symbol = getSymbolLong("TPTP.L");
		symbol = Security.getSymbolString(security);
		System.out.println(symbol);
	}
}

