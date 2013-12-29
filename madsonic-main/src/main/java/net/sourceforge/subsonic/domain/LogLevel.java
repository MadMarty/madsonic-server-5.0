package net.sourceforge.subsonic.domain;

public enum LogLevel {
	
	OFF   (0),
	WARN  (1),
	ERROR (2),
	DEBUG (3),
	INFO  (4), 
	TEST  (5);
	    
	private int code; 
	
	private LogLevel(int c){
		code = c;
	}
	
	public int getLogLevelCode() {
		return code;
	}
}	
