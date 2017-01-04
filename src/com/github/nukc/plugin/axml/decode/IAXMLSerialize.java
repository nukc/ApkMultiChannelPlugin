package com.github.nukc.plugin.axml.decode;

import java.io.IOException;

public interface IAXMLSerialize {
	public int getSize();
	public int getType();
	
	public void setSize(int size);
	public void setType(int type);
	
	public void read(IntReader reader) throws IOException;
	public void write(IntWriter writer) throws IOException;
}
