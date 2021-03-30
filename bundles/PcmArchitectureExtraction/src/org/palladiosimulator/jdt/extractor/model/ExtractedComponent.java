package org.palladiosimulator.jdt.extractor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtractedComponent {
	
	private String name;	
	
	private Map<String, ExtractedInterface> provides = new HashMap<String, ExtractedInterface>();
	private Map<String, ExtractedInterface> requires = new HashMap<String, ExtractedInterface>();
	private List<String> annotations = new ArrayList<>();
	
	public ExtractedComponent(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Map<String, ExtractedInterface> getProvides() {
		return provides;
	}
	
	public Map<String, ExtractedInterface> getRequires() {
		return requires;
	}
	
	public List<String> getAnnotations() {
		return annotations;
	}
	
	public void addAnnotation(String annotationName) {
		if(!annotations.contains(annotationName)) {
			annotations.add(annotationName);
		}
	}

}
