package dev.roanh.gmark.core;

import org.w3c.dom.Element;

public abstract interface Distribution{

	public abstract DistributionType getType();
	
	public static Distribution fromXML(Element data){
		return DistributionType.getByName(data.getAttribute("type")).newInstane(data);
	}
}
