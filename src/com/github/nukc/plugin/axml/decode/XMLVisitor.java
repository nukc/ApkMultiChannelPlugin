package com.github.nukc.plugin.axml.decode;

import com.github.nukc.plugin.axml.decode.BTagNode.Attribute;
import com.github.nukc.plugin.axml.utils.TypedValue;

public class XMLVisitor implements IVisitor{
	private StringBlock mStrings;
	private ResBlock mRes;
	
	private int depth;
	
	public XMLVisitor(StringBlock sb){
		mStrings = sb;
	}

	/**
	 * print header
	 * print child
	 * print tail
	 */
	@Override
	public void visit(BNSNode node) {
		int prefix = node.getPrefix();
		int uri = node.getUri();

		String line1 = String.format("xmlns:%s=%s", getStringAt(prefix) , getStringAt(uri));
		
		System.out.println(line1);
		
		if(node.hasChild()){
			for(BXMLNode child : node.getChildren()){
				child.accept(this);
			}
		}
	}

	@Override
	public void visit(BTagNode node) {
		if(!node.hasChild()){
			print("<"+ getStringAt(node.getName()));
			printAttribute(node.getAttribute());
			print("/>");
		}else{
			print("<"+ getStringAt(node.getName()));
			depth ++;
			printAttribute(node.getAttribute());
			print(">");
			
			for(BXMLNode child : node.getChildren()){
				child.accept(this);
			}
			depth --;
			print("</" + getStringAt(node.getName()) + ">");
		}
	}
	
	public void visit(BTXTNode node){
		print("Text node");
	}
	
	private void printAttribute(Attribute[] attrs){
		for(Attribute attr : attrs){
			StringBuilder sb = new StringBuilder();
			
			if(attr.hasNamespace()){
				sb.append("android").append(':');
			}
			String name = getStringAt(attr.mName);
			if("id".equals(name)){
				System.out.println("hehe");
			}
			sb.append(name).append('=');
			sb.append('\"').append(getAttributeValue(attr)).append('\"');
			
			print(sb.toString());
		}
	}
	final String intent = "                                ";
	final int step = 4;
	private void print(String str){
		System.out.println(intent.substring(0, depth*step)+str);
	}
	
	private String getStringAt(int index){
		return mStrings.getStringFor(index);
	}
	
	@SuppressWarnings("unused")
	private int getResIdAt(int index){
		//TODO final res result in resources.arsc
		return mRes.getResourceIdAt(index);
	}
	
	private String getAttributeValue(Attribute attr) {
		int type = attr.mType >> 24;
		int data = attr.mValue;
		
		if (type==TypedValue.TYPE_STRING) {
			return mStrings.getStringFor(attr.mString);
		}
		if (type==TypedValue.TYPE_ATTRIBUTE) {
			return String.format("?%s%08X",getPackage(data),data);
		}
		if (type==TypedValue.TYPE_REFERENCE) {
			return String.format("@%s%08X",getPackage(data),data);
		}
		if (type==TypedValue.TYPE_FLOAT) {
			return String.valueOf(Float.intBitsToFloat(data));
		}
		if (type==TypedValue.TYPE_INT_HEX) {
			return String.format("0x%08X",data);
		}
		if (type==TypedValue.TYPE_INT_BOOLEAN) {
			return data!=0?"true":"false";
		}
		if (type==TypedValue.TYPE_DIMENSION) {
			return Float.toString(complexToFloat(data))+
				DIMENSION_UNITS[data & TypedValue.COMPLEX_UNIT_MASK];
		}
		if (type==TypedValue.TYPE_FRACTION) {
			return Float.toString(complexToFloat(data))+
				FRACTION_UNITS[data & TypedValue.COMPLEX_UNIT_MASK];
		}
		if (type>=TypedValue.TYPE_FIRST_COLOR_INT && type<=TypedValue.TYPE_LAST_COLOR_INT) {
			return String.format("#%08X",data);
		}
		if (type>=TypedValue.TYPE_FIRST_INT && type<=TypedValue.TYPE_LAST_INT) {
			return String.valueOf(data);
		}
		return String.format("<0x%X, type 0x%02X>",data,type);
	}
	
	private String getPackage(int id) {
		if (id>>>24==1) {
			return "android:";
		}
		return "";
	}
	
	/////////////////////////////////// ILLEGAL STUFF, DONT LOOK :)
		
	public static float complexToFloat(int complex) {
	return (float)(complex & 0xFFFFFF00)*RADIX_MULTS[(complex>>4) & 3];
	}
	
	private static final float RADIX_MULTS[]={
	0.00390625F,3.051758E-005F,1.192093E-007F,4.656613E-010F
	};
	private static final String DIMENSION_UNITS[]={
	"px","dip","sp","pt","in","mm","",""
	};
	private static final String FRACTION_UNITS[]={
	"%","%p","","","","","",""
	};
}
