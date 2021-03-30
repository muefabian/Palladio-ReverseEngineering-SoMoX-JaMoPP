package org.palladiosimulator.jdt.extractor.model;

import java.util.ArrayList;
import java.util.List;

public class ExtractedInterface {
	
	private String name;
	private List<ExtractedMethod> methods = new ArrayList<ExtractedMethod>();
	
	public ExtractedInterface(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public List<ExtractedMethod> getMethods() {
		return methods;
	}

}
