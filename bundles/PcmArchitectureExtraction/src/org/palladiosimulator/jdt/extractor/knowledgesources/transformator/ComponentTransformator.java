package org.palladiosimulator.jdt.extractor.knowledgesources.transformator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.emftext.language.java.annotations.impl.AnnotationInstanceImpl;
import org.emftext.language.java.classifiers.impl.ConcreteClassifierImpl;
import org.emftext.language.java.containers.impl.CompilationUnitImpl;
import org.emftext.language.java.modifiers.AnnotationInstanceOrModifier;
import org.palladiosimulator.jdt.extractor.knowledgesources.base.KnowledgeSourceBase;

public class ComponentTransformator extends KnowledgeSourceBase {
	
	private List<String> registerKeywords = new ArrayList<String>();
	

	@Override
	public void execute() {
		
		for(String name : blackboard.getDockerComponents()) {
			blackboard.addExtractedComponent(name);
		}
		
		// account for missing components
		if(blackboard.getMissingComponents().size() > 0) {
			for(String name : blackboard.getMissingComponents().keySet()) {
				blackboard.addExtractedComponent(name);
			}
		}
		
		
//		lookupComponents();
		
		for(EObject compUnit : blackboard.getResource().getContents()) {
			
			if(compUnit instanceof CompilationUnitImpl) {
				
				// if compilation unit contains no classes jump to next
				if(((CompilationUnitImpl) compUnit).getClassifiers().size() < 1) continue;
				ConcreteClassifierImpl concreteClass = (ConcreteClassifierImpl) ((CompilationUnitImpl) compUnit).getClassifiers().get(0);				
				
				blackboard.addClassifier(concreteClass.getQualifiedName(), concreteClass);
				
				
				for(AnnotationInstanceOrModifier anno : concreteClass.getAnnotationsAndModifiers()) {
					if(anno instanceof AnnotationInstanceImpl) {
						
						String annotationName = ((AnnotationInstanceImpl)anno).getAnnotation().getName();						
						String componentName = blackboard.getComponentFromClass(concreteClass.getQualifiedName());
						
						if(componentName == null) continue;
						
						blackboard.getExtractedComponent(componentName).addAnnotation(annotationName);
					}
				}
					
				
//				switch(type) {
//					case SPRING:
//						for(AnnotationInstanceOrModifier anno : concreteClass.getAnnotationsAndModifiers()) {
//							if(anno instanceof AnnotationInstanceImpl) {
//								String annotationName = ((AnnotationInstanceImpl)anno).getAnnotation().getName();
////								if(((AnnotationInstanceImpl)anno).getAnnotation().getName().equalsIgnoreCase("component")) {
//								if(registerKeywords.contains(annotationName)) {
//									String name = HelperUtil.createQualifiedName(((CompilationUnitImpl) compUnit).getNamespaces());
////									System.out.println(name);
////									blackboard.addExtractedComponent(Util.getComponentName(name));
//									blackboard.addRegisteredComponent(blackboard.getComponentFromClass(name));
//								}
////								System.out.println(concreteClass.getName() +": " + ((AnnotationInstanceImpl)anno).getAnnotation().getName());
//							}
//						}
//						break;
//					case REST:
//						for(Member member : concreteClass.getMembers()) {
////							if(member.getName().equals(registerKeyword)) {
//							if(registerKeywords.contains(member.getName())) {
//								String name = HelperUtil.createQualifiedName(((CompilationUnitImpl) compUnit).getNamespaces());
//								System.out.println(name + " - > " + blackboard.getComponentFromClass(name));
//								if(blackboard.getComponentFromClass(name) != null) {
//									blackboard.addRegisteredComponent(blackboard.getComponentFromClass(name));
////									blackboard.addExtractedComponent(Util.getComponentName(name));
//								}
//							}
//						}
//						break;
//					default:
//						break;
//				}
			}
		}
		
////		Util.printComponents();
//		System.out.println("before: "+blackboard.getRegisteredComponents().size());
//		blackboard.setRegisteredComponents(HelperUtil.checkForDuplicates(blackboard.getRegisteredComponents()));
//		System.out.println("after "+blackboard.getRegisteredComponents().size());
//		
				
		
//		for(String name : blackboard.getDockerComponents()) {
//			blackboard.addExtractedComponent(name);
//		}
//		
//		// account for missing components
//		if(blackboard.getMissingComponents().size() > 0) {
//			for(String name : blackboard.getMissingComponents().keySet()) {
//				blackboard.addExtractedComponent(name);
//			}
//		}		
	}
	
//	private void lookupComponents() {
//		
//		HelperUtil.addComponent("REGISTRY", "tools.descartes.teastore.registry");
//		
//		for(EObject compUnit : blackboard.getResource().getContents()) {
//			if(compUnit instanceof CompilationUnitImpl) {
//			
//				// if compilation unit contains no classes jump to next
//				if(((CompilationUnitImpl) compUnit).getClassifiers().size() < 1) continue;
//				
//				ConcreteClassifierImpl concreteClass = (ConcreteClassifierImpl) ((CompilationUnitImpl) compUnit).getClassifiers().get(0);
//				if(concreteClass.getName().equals("Service")) {
//					if(concreteClass instanceof EnumerationImpl) {
//						EnumerationImpl enumImpl = (EnumerationImpl) concreteClass;
//						for(EnumConstant constant : enumImpl.getConstants()) {
////							namedComponents.put(constant.getName(), ((StringReferenceImpl)constant.getArguments().get(0)).getValue().replace("\"", ""));
//							HelperUtil.addComponent(constant.getName(), ((StringReferenceImpl)constant.getArguments().get(0)).getValue().replace("\"", ""));
//						}
//					}
//				}
//			}
//		}
//	}
	
	public void configure(List<String> registerKeywords) {
		this.registerKeywords = registerKeywords;
	}	
	
//	public static void main(String[] args) {
//		Blackboard blackboard = new Blackboard();
//		ComponentTransformator ext = new ComponentTransformator();
//		ext.configure(blackboard);
//		
//		// load java model from xmi file and save to blackboard
//				ResourceSet resourceSetJava = new ResourceSetImpl();
//				resourceSetJava.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());		
//				ContainersPackage containersPackage = ContainersPackage.eINSTANCE;
//				Resource resourceJava = resourceSetJava.getResource(URI.createFileURI("C:/Users/Fabian/git/Palladio-Supporting-EclipseJavaDevelopmentTools/bundles/jamopp.standalone/msdemo_full.xmi"), true);
//				blackboard.setResource(resourceJava);
//		
//		ext.execute();
//	}
}
