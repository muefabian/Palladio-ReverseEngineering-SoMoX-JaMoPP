package org.palladiosimulator.jdt.extractor.model;

import org.emftext.language.java.types.TypeReference;
import org.palladiosimulator.pcm.repository.DataType;

public class ExtractedParameter {
	
	private String name;
	private TypeReference type;
	
	public ExtractedParameter(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public TypeReference getType() {
		return type;
	}
	
	public void setType(TypeReference type) {
		this.type = type;
	}
}
