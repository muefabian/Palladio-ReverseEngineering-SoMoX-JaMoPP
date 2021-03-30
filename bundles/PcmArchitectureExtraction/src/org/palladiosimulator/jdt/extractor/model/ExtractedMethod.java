package org.palladiosimulator.jdt.extractor.model;

import java.util.ArrayList;
import java.util.List;

import org.emftext.language.java.types.TypeReference;
import org.palladiosimulator.pcm.repository.DataType;

public class ExtractedMethod {
	
	private String name;
//	private DataType returnType;
	private TypeReference returnType;
	private List<ExtractedParameter> parameters = new ArrayList<ExtractedParameter>();
	
	public ExtractedMethod(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public TypeReference getReturnType() {
		return returnType;
	}
	
	public void setReturnType(TypeReference returnType) {
		this.returnType = returnType;
	}
	
	public List<ExtractedParameter> getParameters() {
		return parameters;
	}

}
