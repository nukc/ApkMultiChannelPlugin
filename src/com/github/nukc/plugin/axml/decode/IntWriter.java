package com.github.nukc.plugin.axml.decode;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IntWriter {
	public IntWriter() {
    }
    public IntWriter(OutputStream stream,boolean bigEndian) {
            reset(stream,bigEndian);
    }
    
    public final void reset(OutputStream stream,boolean bigEndian) {
            m_stream=stream;
            m_bigEndian=bigEndian;
            m_position=0;
            
            ByteOrder order = m_bigEndian ?  ByteOrder.BIG_ENDIAN: ByteOrder.LITTLE_ENDIAN;
            shortBB.order(order);
            intBB.order(order);
    }

    public final void close() {
            if (m_stream==null) {
                    return;
            }
            try {
            		m_stream.flush();
                    m_stream.close();
            }
            catch (IOException e) {
            }
            reset(null,false);
    }
    
    public final OutputStream getStream() {
            return m_stream;
    }
    
    public final boolean isBigEndian() {
            return m_bigEndian;
    }
    public final void setBigEndian(boolean bigEndian) {
            m_bigEndian=bigEndian;
    }

    public final void writeByte(byte b) throws IOException {
    	m_stream.write(b);
    	m_position += 1;
    }
    
    public final int writeShort(short s) throws IOException {
    	shortBB.clear();
    	shortBB.putShort(s);
  
    	m_stream.write(shortBB.array());
    	m_position += 2;
    	
    	return 2;
    }
    
    public final int writeInt(int i) throws IOException {
        intBB.clear();
        intBB.putInt(i);
        
        m_stream.write(intBB.array());
        m_position += 4;
        
        return 4;
    }
    
    public final void writeIntArray(int[] array) throws IOException {
         for(int i : array){
        	 writeInt(i);
         }
    }
    
    public final void writeIntArray(int[] array,int offset,int length) throws IOException {
        int limit = offset + length;    
    	for(int i = offset; i< limit; i++){
    		writeInt(i);
    	}
    }
    
    public final int writeByteArray(byte[] array) throws IOException {
    	m_stream.write(array);
    	m_position += array.length;
    	
    	return array.length;
    }
    
    public final void skip(int n, byte def) throws IOException {
    	for(int i =0; i< n; i++){
    		m_stream.write(def);
    	}
    	
    	m_position += n;
    }
    
    public final void skipIntFFFF() throws IOException {
    	writeInt(Integer.MAX_VALUE);
    }
    
    public final void skipInt0000() throws IOException {
    	writeInt(0);
    }
    
    public final int getPosition() {
            return m_position;
    }
    
    /////////////////////////////////// data

    private OutputStream m_stream;
    private boolean m_bigEndian;
    private int m_position;
    
    private ByteBuffer shortBB = ByteBuffer.allocate(2);
    private ByteBuffer intBB = ByteBuffer.allocate(4);
}
