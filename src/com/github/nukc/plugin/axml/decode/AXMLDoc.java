package com.github.nukc.plugin.axml.decode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static java.lang.System.out;

public class AXMLDoc {
	private final int MAGIC_NUMBER = 0X00080003;
	private final int CHUNK_STRING_BLOCK = 0X001C0001;
	private final int CHUNK_RESOURCE_ID = 0X00080180;
	private final int CHUNK_XML_TREE = 0X00100100;
	
	private final String MANIFEST 	 = "manifest";
	private final String APPLICATION = "application";
	
	private int mDocSize ;
	private StringBlock mStringBlock;
	private ResBlock mResBlock;
	
	private BXMLTree mXMLTree;
	
	public AXMLDoc(){
	}

	public StringBlock getStringBlock(){
		return mStringBlock;
	}
	
	public ResBlock getResBlock(){
		return mResBlock;
	}
	
	public BXMLTree getBXMLTree(){
		return mXMLTree;
	}
	
	public BXMLNode getManifestNode(){
		List<BXMLNode> children = mXMLTree.getRoot().getChildren();
		
		for(BXMLNode node : children){
			if( MANIFEST.equals( mStringBlock.getStringFor(((BTagNode)node).getName() ) )){
				return node;
			}
		}
		
		return null;
	}
	
	public BXMLNode getApplicationNode(){
		BXMLNode manifest = getManifestNode();
		if(manifest == null){
			return null;
		}
		
		for(BXMLNode node : manifest.getChildren()){
			if( APPLICATION.equals( mStringBlock.getStringFor(((BTagNode)node).getName() ))){
				return node;
			}
		}
		
		return null;
	}
	
	/**
	 * Prepare() should be called, if any resource has changes.
	 * @param os
	 * @throws IOException
	 */
	public void build(OutputStream os) throws IOException{
		IntWriter writer = new IntWriter(os, false);
		mStringBlock.prepare();
		mResBlock.prepare();
		mXMLTree.prepare();
		
		int base = 8;
		mDocSize = base + mStringBlock.getSize() + mResBlock.getSize() + mXMLTree.getSize();
		
		writer.writeInt(MAGIC_NUMBER);
		writer.writeInt(mDocSize);
		
		mStringBlock.write(writer);
		mResBlock.write(writer);
		mXMLTree.write(writer);
		
		os.flush();
		os.close();
	}
	
	public void testSize() throws IOException{
		out.println("string block size:" + mStringBlock.getSize());
		mStringBlock.prepare();
		out.println("string block size:" + mStringBlock.getSize());
		
		out.println("res block size:" + mResBlock.getSize());
		mResBlock.prepare();
		out.println("res size:" + mResBlock.getSize());
		
		out.println("xml size:" + mXMLTree.getSize());
		mXMLTree.prepare();
		out.println("xml size:" + mXMLTree.getSize());
		
		out.println("doc size:" + mDocSize);
		int base = 8;
		int size = base + mStringBlock.getSize() + mResBlock.getSize() + mXMLTree.getSize();
		out.println("doc size:" + size);
	}
	
	public void print(){
		out.println("size:" + mDocSize);
		mXMLTree.print(new XMLVisitor(mStringBlock));
	}
	
	public void parse(InputStream is) throws Exception{
		IntReader reader = new IntReader(is, false);
		
		int magicNum = reader.readInt();
		
		if(magicNum != MAGIC_NUMBER){
			throw new RuntimeException("Not valid AXML format");
		}
		
		int size = reader.readInt();
		
		mDocSize = size;
		
		int chunkType = reader.readInt();
		
		if(chunkType == CHUNK_STRING_BLOCK){
			parseStringBlock(reader);
		}
		
		chunkType = reader.readInt();
		
		if(chunkType == CHUNK_RESOURCE_ID){
			parseResourceBlock(reader);
		}

		chunkType = reader.readInt();
		
		if(chunkType == CHUNK_XML_TREE){
			parseXMLTree(reader);
		}
	}
	
	private void parseStringBlock(IntReader reader) throws Exception{
		StringBlock block = new StringBlock();
		block.read(reader);
		
		mStringBlock = block;
	}
	
	private void parseResourceBlock(IntReader reader) throws IOException{
		ResBlock block = new ResBlock();
		block.read(reader);
		mResBlock = block;
	}
	
	private void parseXMLTree(IntReader reader) throws Exception{
		BXMLTree tree = new BXMLTree();
		tree.read(reader);
		
		mXMLTree = tree;
	}
}
