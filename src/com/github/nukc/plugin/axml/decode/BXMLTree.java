package com.github.nukc.plugin.axml.decode;

import com.github.nukc.plugin.axml.utils.Pair;

import java.io.IOException;
import java.util.Stack;

public class BXMLTree implements IAXMLSerialize{
	private final int NS_START  = 0x00100100;
	private final int NS_END  	= 0x00100101;
	private final int NODE_START= 0x00100102;
	private final int NODE_END  = 0x00100103;
	private final int TEXT		= 0x00100104;
	
	private Stack<BXMLNode> mVisitor;
	private BNSNode mRoot;
	private int mSize;

	public BXMLTree(){
		mRoot = new BNSNode();
		mVisitor = new Stack<BXMLNode>();
	}
	
	public void print(IVisitor visitor){
		mRoot.accept(visitor);
	}
	
	public void write(IntWriter writer) throws IOException{
		write(mRoot, writer);
	}
	
	public void prepare(){
		mSize = 0;
		prepare(mRoot);
	}
	
	private void write(BXMLNode node, IntWriter writer) throws IOException{
		node.writeStart(writer);
		
		if(node.hasChild()){
			for(BXMLNode child : node.getChildren()){
				write(child, writer);
			}
		}
		node.writeEnd(writer);
	}
	
	private void prepare(BXMLNode node){
		node.prepare();
		Pair<Integer,Integer> p = node.getSize();
		mSize += p.first + p.second;
		
		if(node.hasChild()){
			for(BXMLNode child:node.getChildren()){
				prepare(child);
			}
		}
	}

	public int getSize(){
		return mSize;
	}
	
	public BXMLNode getRoot(){
		return mRoot;
	}
	
	public void read(IntReader reader) throws IOException{
		mRoot.checkStartTag(NS_START);
		mVisitor.push(mRoot);
		mRoot.readStart(reader);
		
		int chunkType;
		
		end:while(true){
			chunkType = reader.readInt();
			
			switch(chunkType){
			case NODE_START:
			{
				BTagNode node = new BTagNode();
				node.checkStartTag(NODE_START);
				BXMLNode parent = mVisitor.peek();
				parent.addChild(node);
				mVisitor.push(node);
				
				node.readStart(reader);
			}
			break;
			case NODE_END:
			{
				BTagNode node = (BTagNode)mVisitor.pop();
				node.checkEndTag(NODE_END);
				node.readEnd(reader);
			}
			break;
			case TEXT:
			{
				System.out.println("Hello Text");
				
			}
			break;
			case NS_END:
				break end;
			}
		}
		
		if( !mRoot.equals(mVisitor.pop())){
			throw new IOException("doc has invalid end");
		}
		
		mRoot.checkEndTag(chunkType);
		mRoot.readEnd(reader);
	}

	@Override
	public int getType() {
		return 0;
	}

	@Override
	public void setSize(int size) {
	}

	@Override
	public void setType(int type) {	
	}
}
