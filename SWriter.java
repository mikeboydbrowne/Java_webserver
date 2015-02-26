package edu.upenn.cis.cis455.webserver;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

public class SWriter extends PrintWriter {
	
	HashMap<String, String> 	headers 	= new HashMap<String,String>();
	HashMap<String, String> 	cookieVals 	= new HashMap<String,String>();
	HSession					session;
	StringBuffer 				buffer;
	OutputStream 				out;
	String						statusText;
	boolean						errorOccured;
	boolean						committed;
	int							bufferSize;
	int							status;


	public SWriter(OutputStream out, HSession s) {
		super(out);
		this.buffer 		= new StringBuffer();
		this.session		= s;
		this.errorOccured 	= false;
		this.committed		= false;
		this.out 			= out;
		bufferSize			= 500;
		status				= 200;
		statusText			= "OK";
	}
	
	@Override
	public void flush() {
		try {
			if (!committed) {
				// writing status line
				String reqStatus = "HTTP/1.1 " + status + " " + statusText + "\r\n";
				out.write(reqStatus.getBytes());
				// writing headers
				for (String s : headers.keySet()) {
					String headerText = "";
					headerText = s + ": " + headers.get(s) + "\r\n";
					out.write(headerText.getBytes());
				}
				// writing cookie values
				if (!session.isInvalid()) {
					if (session.isNew()) {
						for (String t : cookieVals.keySet()) {
							String cookieText = "Set-Cookie: " + t + "=" + cookieVals.get(t) + "\r\n";
							out.write(cookieText.getBytes());
						}
					} else {
						for (String t : cookieVals.keySet()) {
							String cookieText = "Cookie: " + t + "=" + cookieVals.get(t) + "\r\n";
							out.write(cookieText.getBytes());
						}
					}
				}
				// writing the buffer
				out.write("\r\n".getBytes());
				out.write(buffer.toString().getBytes());
				committed = true;
			} else {
				out.write(buffer.toString().getBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		clearBuffer();
	}
	
	@Override
	public void println(String x) {
		buffer.append(x + "\r\n");
	}
	
	public void clearBuffer() {
		buffer = new StringBuffer();
	}
	
	@Override
	public SWriter append(char c) {
		// enough room in the buffer
		if (buffer.length() < bufferSize - 1) {
			buffer.append(c);
		
		// not enough room in the buffer
		} else {
			flush();	// flushing the buffer b/c it's reached its size
			buffer.append(c);
		}		
		return this;
	}
	
	@Override
	public SWriter append(CharSequence csq) {
		// enough room in the buffer
		if (buffer.length() < bufferSize - csq.length()) {
			buffer.append(csq);
		
		// not enough room in the buffer
		} else {
			buffer.append(csq.subSequence(0, bufferSize - csq.length()));
			flush();	// flushing the buffer b/c it's reached its size
			buffer.append(csq.subSequence(bufferSize - csq.length() + 1, csq.length()));
		}
		return this;
	}
	
	@Override
	public SWriter append(CharSequence csq, int start, int end) {
		// enough room in the buffer
		if (buffer.length() < bufferSize - end + start) {
			buffer.append(csq.subSequence(start, end));
		
		// not enough room in the buffer
		} else {
			buffer.append(csq.subSequence(start, bufferSize - end + start));
			flush();	// flushing the buffer b/c it's reached its size
			buffer.append(csq.subSequence(bufferSize - end + start, end));
		}
		return this;
	}
	
	@Override
	public boolean checkError() {
		return errorOccured;
	}
	
	@Override
	protected void clearError() {
		errorOccured = false;
	}
	
	@Override
	public void close() {
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void setError() {
		errorOccured = true;
	
	}

	@Override
	public void write(char[] buf) {
 		if (buffer.length() + buf.length > bufferSize) {
 			buffer.append(buf.toString().subSequence(0, buffer.length() + buf.length - bufferSize));
 			flush();
 			buffer.append(buf.toString().subSequence(buffer.length() + buf.length - bufferSize + 1, buf.length));
 		} else {
 			buffer.append(buf);
 		}
 	}

	@Override
	public void write(char[] buf, int off, int len) {
		if (buffer.length() + len > bufferSize) {
			for (int i = off; i <= buffer.length() + len - bufferSize; i++) {
				buffer.append(buf[i]);
			}
			flush();
			for(int i = buffer.length() + len - bufferSize; i <= buffer.length() + 2 * len - bufferSize; i++) {
				buffer.append(buf[i]);
			}
		} else {
			buffer.append(buf);
		}
		
	}
	
	@Override
	public void write(int c) {
		if (buffer.length() + 1 > bufferSize) {
			flush();
			buffer.append((char) c);
		} else {
			buffer.append((char) c);
		}
	}
	
	@Override
	public void write(String s) {
		if (buffer.length() + s.length() > bufferSize) {
 			buffer.append(s.subSequence(0, buffer.length() + s.length() - bufferSize));
 			flush();
 			buffer.append(s.subSequence(buffer.length() + s.length() - bufferSize + 1, s.length()));
 		} else {
 			buffer.append(s);
 		}	
	}
	
	@Override
	public void write(String s, int off, int len) {
		if (buffer.length() + len > bufferSize) {
 			buffer.append(s.subSequence(off, buffer.length() + len - bufferSize + off));
 			flush();
 			buffer.append(s.subSequence(buffer.length() + len - bufferSize + off + 1, buffer.length() - bufferSize + off + 1));
 		} else {
 			buffer.append(s);
 		}	
	}
	
	public int setBufferSize(int length) {
		bufferSize = length;		// set the 
		return 1;
	}
	
	public int setStatus(int arg0) {
		status = arg0;
		if (arg0 == 200) {
			statusText = "OK";
		} else if (arg0 == 302) {
			statusText = "Page Found";
		} else if (arg0 == 403) {
			statusText = "Access Forbidden";
		} else if (arg0 == 404) {
			statusText = "Page Not Found";
		} else if (arg0 == 500) {
			statusText = "Internal Server Error";
		}
		return 1;
	}
	
	public int setStatus(int arg0, String arg1) {
		status = arg0;
		statusText = arg1;
		return 1;
	}
	
	
}
