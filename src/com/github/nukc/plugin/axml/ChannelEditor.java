package com.github.nukc.plugin.axml;

import com.github.nukc.plugin.axml.decode.AXMLDoc;
import com.github.nukc.plugin.axml.decode.BTagNode;
import com.github.nukc.plugin.axml.decode.BTagNode.Attribute;
import com.github.nukc.plugin.axml.decode.BXMLNode;
import com.github.nukc.plugin.axml.decode.StringBlock;
import com.github.nukc.plugin.axml.utils.TypedValue;

import java.util.List;

public class ChannelEditor {
    private final String NAME_SPACE = "http://schemas.android.com/apk/res/android";
    private final String META_DATA = "meta-data";
    private final String NAME = "name";
    private final String VALUE = "value";
    private final String UMENG_CHANNEL = "UMENG_CHANNEL";

    private String mChannelValue = "channel";

    private int namespace;
    private int meta_data;
    private int attr_name;
    private int attr_value;
    private int channel_name;
    private int channel_value = -1;

    private AXMLDoc mDoc;

    public ChannelEditor(AXMLDoc doc) {
        this.mDoc = doc;
    }

    public void setChannel(String channel) {
        mChannelValue = channel;
    }

    //First add resource and get mapping ids
    private void registStringBlock(StringBlock sb) {
        namespace = sb.putString(NAME_SPACE);
        meta_data = sb.putString(META_DATA);
        attr_name = sb.putString(NAME);
        attr_value = sb.putString(VALUE);
        channel_name = sb.putString(UMENG_CHANNEL);

        if (channel_value == -1) {
            channel_value = sb.addString(mChannelValue);//now we have a seat in StringBlock
        }
    }

    //put string to the seat
    private void replaceValue(StringBlock sb) {
        sb.setString(channel_value, mChannelValue);
    }

    //Second find&change meta-data's value or add a new one
    private void editNode(AXMLDoc doc) {
        BXMLNode application = doc.getApplicationNode(); //manifest node
        List<BXMLNode> children = application.getChildren();

        BTagNode umeng_meta = null;

        end:
        for (BXMLNode node : children) {
            BTagNode m = (BTagNode) node;
            //it's a risk that the value for "android:name" maybe not String
            if ((meta_data == m.getName()) && (m.getAttrStringForKey(attr_name) == channel_name)) {
                umeng_meta = m;
                break end;
            }
        }

        if (umeng_meta != null) {
            umeng_meta.setAttrStringForKey(attr_value, channel_value);
        } else {
            Attribute name_attr = new Attribute(namespace, attr_name, TypedValue.TYPE_STRING);
            name_attr.setString(channel_name);
            Attribute value_attr = new Attribute(namespace, attr_value, TypedValue.TYPE_STRING);
            value_attr.setString(channel_value);

            umeng_meta = new BTagNode(-1, meta_data);
            umeng_meta.setAttribute(name_attr);
            umeng_meta.setAttribute(value_attr);

            children.add(umeng_meta);
        }
    }

    public void commit() {
        registStringBlock(mDoc.getStringBlock());
        editNode(mDoc);

        replaceValue(mDoc.getStringBlock());
    }
}
