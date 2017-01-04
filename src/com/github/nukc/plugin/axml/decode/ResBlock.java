package com.github.nukc.plugin.axml.decode;

import java.io.IOException;

public class ResBlock implements IAXMLSerialize{
	private static final int TAG = 0x00080180;
	
	private int mChunkSize;
	private int[] mRawResIds;
	
	public void print(){
		StringBuilder sb = new StringBuilder();
		
		for(int id : getResourceIds()){
			sb.append(id);
			sb.append(" ");
		}
		
		System.out.println(sb.toString());
	}
	
	public void read(IntReader reader) throws IOException{
		mChunkSize = reader.readInt();
		
		if(mChunkSize < 8 || (mChunkSize % 4)!= 0){
			throw new IOException("Invalid resource ids size ("+mChunkSize+").");
		}
		
		mRawResIds = reader.readIntArray(mChunkSize/4 - 2);//subtract base offset (type + size)
	}
	
	private final int INT_SIZE = 4;
	public void prepare(){
		int base = 2*INT_SIZE;
		int resSize = mRawResIds == null ? 0:mRawResIds.length*INT_SIZE;
		mChunkSize = base + resSize;
	}
	
	@Override
	public void write(IntWriter writer) throws IOException {
		writer.writeInt(TAG);
		writer.writeInt(mChunkSize);
		
		if(mRawResIds != null){
			for(int id : mRawResIds){
				writer.writeInt(id);
			}
		}
	}
	
	public int[] getResourceIds(){
		return mRawResIds;
	}
	
	public int getResourceIdAt(int index){
		return mRawResIds[index];
	}

	@Override
	public int getSize() {
		return mChunkSize;
	}

	@Override
	public int getType() {
		return TAG;
	}

	@Override
	public void setSize(int size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setType(int type) {
		// TODO Auto-generated method stub
		
	}
}
