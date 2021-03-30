package org.palladiosimulator.jdt.extractor.knowledgesources.transformator;


import org.emftext.language.java.annotations.impl.AnnotationInstanceImpl;
import org.emftext.language.java.annotations.impl.AnnotationParameterImpl;
import org.emftext.language.java.annotations.impl.SingleAnnotationParameterImpl;
import org.emftext.language.java.classifiers.impl.ConcreteClassifierImpl;
import org.emftext.language.java.members.Member;
import org.emftext.language.java.members.impl.ClassMethodImpl;
import org.emftext.language.java.members.impl.MethodImpl;
import org.emftext.language.java.modifiers.AnnotationInstanceOrModifier;
import org.emftext.language.java.parameters.Parameter;
import org.emftext.language.java.references.impl.StringReferenceImpl;
import org.emftext.language.java.types.impl.ClassifierReferenceImpl;
import org.emftext.language.java.types.impl.TypesFactoryImpl;
import org.palladiosimulator.jdt.extractor.knowledgesources.base.KnowledgeSourceBase;
import org.palladiosimulator.jdt.extractor.model.ExtractedMethod;
import org.palladiosimulator.jdt.extractor.model.ExtractedParameter;
import org.palladiosimulator.jdt.extractor.util.ResolverUtil;


public class InterfaceTransformator extends KnowledgeSourceBase {

	@Override
	public void execute() {
		
		for(String component : blackboard.getDockerComponents()) {
			blackboard.addExtractedInterface(component);
		}
		
		for(ConcreteClassifierImpl concreteClass : blackboard.getAllClassifiers().values()) {			
			
			for(AnnotationInstanceOrModifier anno : concreteClass.getAnnotationsAndModifiers()) {
				if(anno instanceof AnnotationInstanceImpl) {
					
					String annotationName = ((AnnotationInstanceImpl)anno).getAnnotation().getName();
					
					
					if(annotationName.equalsIgnoreCase("Controller") || annotationName.equalsIgnoreCase("RestController") || annotationName.equalsIgnoreCase("Path") || annotationName.equalsIgnoreCase("WebServlet")) {
						String componentName = blackboard.getComponentFromClass(concreteClass.getQualifiedName());	
						
						if(componentName != null) {
							blackboard.addComponentControllerClass(componentName, concreteClass);
						}
						else {
							blackboard.addOrphanControllerClass(concreteClass.getQualifiedName(), concreteClass);
						}
					}
				}
			}
		}
		
		
		blackboard.getOrphanControllerClasses().entrySet().forEach(entry -> {
			
			ResolverUtil resolverUtil = new ResolverUtil(blackboard, type);
			
			String component = resolverUtil.resolveAnnotationDependencies(entry.getKey());
			
			if(component != null) {
				blackboard.addComponentControllerClass(component, entry.getValue());
			}
		});
		
		
		blackboard.getComponentControllerClasses().entrySet().forEach(entry -> {
			for(ConcreteClassifierImpl concreteClass : entry.getValue()) {
				
				for(AnnotationInstanceOrModifier anno : concreteClass.getAnnotationsAndModifiers()) {
					if(anno instanceof AnnotationInstanceImpl) {
						
						String annotationName = ((AnnotationInstanceImpl)anno).getAnnotation().getName();
						
						if(annotationName.equals("WebServlet")) {

							AnnotationParameterImpl annoParam = (AnnotationParameterImpl) ((AnnotationInstanceImpl)anno).getParameter();
							String paramValue = null;
							
							if(annoParam != null) {
								if(annoParam instanceof SingleAnnotationParameterImpl) {
									if(((SingleAnnotationParameterImpl)annoParam).getValue() instanceof StringReferenceImpl) {
										StringReferenceImpl value = (StringReferenceImpl) ((SingleAnnotationParameterImpl)annoParam).getValue();
										paramValue = value.getValue();
										if(paramValue.startsWith("/")) {
											paramValue = paramValue.substring(1);
										}
									}
									
								}
							}
							
							for(Member member : concreteClass.getMembers()) {
								if(member instanceof ClassMethodImpl) {									
									if(((ClassMethodImpl) member).isPublic() || ((ClassMethodImpl) member).isProtected()) {
										
										if(paramValue != null) {
											addInterfaceMethodWithPath(entry.getKey(), (MethodImpl) member, paramValue);
										}
										else {
											addInterfaceMethod(entry.getKey(), (MethodImpl) member);
										}
									}									
								}
							}
						}
						else if(annotationName.equals("Path")) {
							
							AnnotationParameterImpl annoParam = (AnnotationParameterImpl) ((AnnotationInstanceImpl)anno).getParameter();
							String[] paramValue = new String[1];
							paramValue[0] = null;
							
							if(annoParam != null) {
								if(annoParam instanceof SingleAnnotationParameterImpl) {
									if(((SingleAnnotationParameterImpl)annoParam).getValue() instanceof StringReferenceImpl) {
										StringReferenceImpl value = (StringReferenceImpl) ((SingleAnnotationParameterImpl)annoParam).getValue();
										paramValue[0] = value.getValue();
										if(paramValue[0].startsWith("/")) {
											paramValue[0] = paramValue[0].substring(1);
										}
									}
									
								}
							}
							
							for(Member member : concreteClass.getMembers()) {
								if(member instanceof MethodImpl) {
									
									((MethodImpl) member).getAnnotationsAndModifiers().forEach(e -> {
										
										if(e instanceof AnnotationInstanceImpl) {
											
											String methodAnnotationName = ((AnnotationInstanceImpl) e).getAnnotation().getName();
											
											if(isRestMethod(methodAnnotationName)) {
												if(paramValue[0] != null) {
													addInterfaceMethodWithPath(entry.getKey(), (MethodImpl) member, paramValue[0]);
												}
												else {
													addInterfaceMethod(entry.getKey(), (MethodImpl) member);
												}
											}
										}
									});
								}
							}							
						}
						
						else if(annotationName.equals("Controller") || annotationName.equals("RestController")){
							for(Member member : concreteClass.getMembers()) {
								if(member instanceof MethodImpl) {
									
									((MethodImpl) member).getAnnotationsAndModifiers().forEach(e -> {
										
										if(e instanceof AnnotationInstanceImpl) {
											
											String methodAnnotationName = ((AnnotationInstanceImpl) e).getAnnotation().getName();
											
											if(isRestMethod(methodAnnotationName)) {
												addInterfaceMethod(entry.getKey(), (MethodImpl) member);
											}
										}
									});
								}
							}
						}
					}
				}							
			}
		});	
			
		
		// add extracted interfaces to related component
		blackboard.getExtractedInterfaces().entrySet().forEach(entry -> {
			blackboard.getExtractedComponent(entry.getKey()).getProvides().put(entry.getKey(), entry.getValue());
		});
		
		
		if(blackboard.getMissingComponents().size() > 0) {
			for(String name : blackboard.getMissingComponents().keySet()) {
				blackboard.addExtractedInterface(name);
			}
		}		
		System.out.println();
	}
	
	public void addInterfaceMethod(String interfaceName, MethodImpl method) {
		
		
		ExtractedMethod newMethod = new ExtractedMethod(method.getName());
		newMethod.setReturnType(method.getTypeReference());
	
		for(Parameter param : method.getParameters()) {
			ExtractedParameter newParameter = new ExtractedParameter(param.getName());
			newParameter.setType(param.getTypeReference());
			newMethod.getParameters().add(newParameter);
		}
		
		blackboard.getExtractedInterface(interfaceName).getMethods().add(newMethod);
		
	}
	
	public void addInterfaceMethodWithPath(String interfaceName, MethodImpl method, String path) {
		
		
		
		ExtractedMethod newMethod = new ExtractedMethod(method.getName());
		newMethod.setReturnType(method.getTypeReference());
		
		ExtractedParameter pathParam = new ExtractedParameter(path);
		
		ClassifierReferenceImpl classRef = (ClassifierReferenceImpl) TypesFactoryImpl.eINSTANCE.createClassifierReference();
		classRef.setTarget(blackboard.getAllClassifiers().get("java.lang.String"));		
		pathParam.setType(classRef);
		
		newMethod.getParameters().add(pathParam);
	
		for(Parameter param : method.getParameters()) {
			ExtractedParameter newParameter = new ExtractedParameter(param.getName());
			newParameter.setType(param.getTypeReference());
			newMethod.getParameters().add(newParameter);
		}
		
		blackboard.getExtractedInterface(interfaceName).getMethods().add(newMethod);
		
	}
	
	private boolean isRestMethod(String annotationName) {
		if(annotationName.equalsIgnoreCase("post") || annotationName.equalsIgnoreCase("get") || annotationName.equalsIgnoreCase("put") 
				|| annotationName.equalsIgnoreCase("delete") || annotationName.equalsIgnoreCase("patch") || annotationName.equalsIgnoreCase("requestmapping") 
				|| annotationName.equalsIgnoreCase("postmapping") || annotationName.equalsIgnoreCase("getmapping") || annotationName.equalsIgnoreCase("putmapping") 
				|| annotationName.equalsIgnoreCase("deletemapping") || annotationName.equalsIgnoreCase("patchmapping")) {
			return true;
		}		
		return false;
	}

}
