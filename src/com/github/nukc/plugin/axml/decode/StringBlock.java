/**
 *  Copyright 2011 Ryszard Wiśniewski <brut.alll@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.nukc.plugin.axml.decode;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * write and read StringBlock
 * @author NTOOOOOP
 */
public class StringBlock implements IAXMLSerialize{
		private static final int TAG = 0x001C0001;
		private static final int INT_SIZE = 4;
		
		private int mChunkSize;
		private int mStringsCount;
		private int mStylesCount;
		private int mEncoder;
		
		private int mStrBlockOffset;
		private int mStyBlockOffset;
		
		private int[] mPerStrOffset;
		private int[] mPerStyOffset;
		
		/**
		 * raw String
		 */
		private List<String> mStrings;
		/**
		 * android can identify HTML tags in a string，all the styles are kept here 
		 */
		private List<Style> mStyles;
		
		public int getStringMapping(String str){
			int size = mStrings.size();
			for(int i=0; i< size ; i++){
				if(mStrings.get(i).equals(str)){
					return i;
				}
			}
			
			return -1;
		}
		
		public int putString(String str){
			if(containsString(str)){
				return getStringMapping(str);
			}
			
			return addString(str);
		}
		
		public int addString(String str){
			mStrings.add(str);
			return ( mStrings.size() - 1);
		}
		
		public String setString(int index, String str){
			return mStrings.set(index, str);
		}
		
		public boolean containsString(String str){
			return mStrings.contains(str.trim());
		}
		
		public int getStringCount(){
			return mStrings.size();
		}
		
        /**
         * Reads whole (including chunk type) string block from stream.
         * Stream must be at the chunk type.
         */
        public void read(IntReader reader) throws IOException {
			mChunkSize = reader.readInt();
			mStringsCount = reader.readInt();
			mStylesCount = reader.readInt();
			
			mEncoder = reader.readInt();//utf-8 or uft16
			
			mStrBlockOffset =reader.readInt();
			mStyBlockOffset =reader.readInt();
			
			if(mStringsCount > 0){
				mPerStrOffset = reader.readIntArray(mStringsCount);
				mStrings = new ArrayList<String>(mStringsCount);
			}
			
			if(mStylesCount > 0){
				mPerStyOffset = reader.readIntArray(mStylesCount);
				mStyles = new ArrayList<Style>();
			}
			
			//read string
			if(mStringsCount >0){
				int size = ((mStyBlockOffset == 0)?mChunkSize:mStyBlockOffset) - mStrBlockOffset;
				byte[] rawStrings = reader.readByteArray(size);
				
				for(int i =0; i < mStringsCount ; i++){
					int offset = mPerStrOffset[i];
		        	short len = toShort(rawStrings[offset], rawStrings[offset+1]);
					mStrings.add(i,new String(rawStrings,offset+2, len*2, Charset.forName("UTF-16LE")));
				}
			}
			
			//read styles
			if(mStylesCount > 0){
				int size = mChunkSize - mStyBlockOffset;
				int[] styles = reader.readIntArray(size/4);
				

				for(int i = 0; i< mStylesCount; i++){
					int offset = mPerStyOffset[i];
					int j = offset;
					for(; j< styles.length; j++){
						if(styles[j] == -1) break;
					}
					
					int[] array = new int[j-offset];
					System.arraycopy(styles, offset, array, 0, array.length);
					Style d = Style.parse(array);
					
					mStyles.add(d);
				}
			}
        }
        
        @Override
		public void write(IntWriter writer) throws IOException {
			//base seven
        	int size = 0;
        	size += writer.writeInt(TAG);
        	size += writer.writeInt(mChunkSize);
        	size += writer.writeInt(mStringsCount);
        	size += writer.writeInt(mStylesCount);
        	size += writer.writeInt(mEncoder);
        	size += writer.writeInt(mStrBlockOffset);
        	size += writer.writeInt(mStyBlockOffset);
			
			if(mPerStrOffset != null){
				for(int offset : mPerStrOffset){
					size += writer.writeInt(offset);
				}
			}
			
			if(mPerStyOffset != null){
				for(int offset : mPerStyOffset){
					size += writer.writeInt(offset);
				}
			}
			
			if(mStrings != null){
				for(String s : mStrings){
					byte[] raw = s.getBytes("UTF-16LE");
					size += writer.writeShort((short)(s.length()));
					size += writer.writeByteArray(raw);
					size += writer.writeShort((short)0);
				}
			}
			
			if(mStyles != null){
				for(Style style : mStyles){
					size += style.write(writer);
				}
			}
			
			if(mChunkSize > size){
				writer.writeShort((short)0);
			}
			
		}
        
        public void prepare() throws IOException{
        	//mStrings
        	mStringsCount = mStrings == null ? 0:mStrings.size();
        	mStylesCount = mStyles == null ? 0: mStyles.size();
        	
        	//string & style block offset
        	int base = INT_SIZE*7;//from 0 to string array
        	
			int strSize = 0;
			int []perStrSize = null;
			
			if(mStrings != null){
				int size = 0;
				perStrSize = new int[mStrings.size()];
				for(int i =0; i< mStrings.size(); i++){
					perStrSize[i] = size;
					try{
						size += 2 + mStrings.get(i).getBytes("UTF-16LE").length + 2;
					}catch(UnsupportedEncodingException e){
						throw new IOException(e);
					}
				}
				strSize = size;
			}
			
			int stySize = 0;
			int[] perStySize = null;
			if(mStyles != null){
				int size = 0;
				perStySize = new int[mStyles.size()];
				for(int i=0; i< mStyles.size(); i++){
					perStySize[i] = size;
					size += mStyles.get(i).getSize();
				}
				stySize = size;
			}
			
			int string_array_size = perStrSize == null ? 0: perStrSize.length*INT_SIZE;
			int style_array_size = perStySize == null ? 0: perStySize.length*INT_SIZE;
			
			if(mStrings!= null && mStrings.size() >0){
				mStrBlockOffset = base + string_array_size + style_array_size;
				mPerStrOffset = perStrSize;
			}else{
				mStrBlockOffset = 0;
				mPerStrOffset = null;
			}
			
			if(mStyles != null && mStyles.size() > 0){
				mStyBlockOffset = base + string_array_size + style_array_size + strSize;
				mPerStyOffset = perStySize;
			}else{
				mStyBlockOffset = 0;
				mPerStyOffset = null;
			}
			
			mChunkSize = base + string_array_size + style_array_size + strSize + stySize;
			
			int align = mChunkSize % 4;
			if(align != 0){
				mChunkSize += (INT_SIZE - align);
			}
        }
        
        public int getSize(){
        	return mChunkSize;
        }
        
        public String getStringFor(int index){
        	return mStrings.get(index);
        }
        
        private short toShort(short byte1, short byte2)
        {
            return (short)((byte2 << 8) + byte1);
        }
        
        public Style getStyle(int index){
        	return mStyles.get(index);
        }

        ///////////////////////////////////////////// implementation

        public StringBlock() {
        }
        
		@Override
		public int getType() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void setSize(int size) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setType(int type) {
			// TODO Auto-generated method stub
			
		}
		
		public static class Style {
			List<Decorator> mDct;
			
			public Style(){
				mDct = new ArrayList<Decorator>();
			}
			
			public List<Decorator> getDecorator(){
				return mDct;
			}
			
			public void addStyle(Decorator style){
				mDct.add(style);
			}
			
			public int getSize(){
				int size = 0;
				size += getCount()* Decorator.SIZE;
				size += 1;//[-1] as a seperator
				return size;
			}
			
			public int getCount(){
				return mDct.size();
			}
			
			public static Style parse(int[] muti_triplet) throws IOException{
				if(muti_triplet == null || (muti_triplet.length% Decorator.SIZE != 0)){
					throw new IOException("Fail to parse style");
				}
				
				Style d = new Style();
				
				Decorator style = null;
				for(int i = 0; i < muti_triplet.length; i++){
					if(i% Decorator.SIZE == 0){
						new Decorator();
					}
					
					switch(i%3){
					case 0:
					{
						style = new Decorator();
						style.mTag = muti_triplet[i];
					}break;
					case 1:
					{
						style.mDoctBegin = muti_triplet[i];
					}break;
					case 2:
					{
						style.mDoctEnd = muti_triplet[i];
						d.mDct.add(style);
					}break;
					}
				}
				
				return d;
			}
			
			public int write(IntWriter writer) throws IOException{
				int size = 0;
				if(mDct!= null && mDct.size() > 0){
					for(Decorator dct: mDct){
						size += writer.writeInt(dct.mTag);
						size += writer.writeInt(dct.mDoctBegin);
						size += writer.writeInt(dct.mDoctEnd);
					}
					
					size += writer.writeInt(-1);
				}
				
				return size;
			}
		}
		
		public static class Decorator{
			public static final int SIZE = 3;
			
			public int mTag;
			public int mDoctBegin;
			public int mDoctEnd;
			
			public Decorator(int[] triplet){
				mTag = triplet[0];
				mDoctBegin = triplet[1];
				mDoctEnd = triplet[2];
			}
			
			public Decorator(){}
			
		}
}