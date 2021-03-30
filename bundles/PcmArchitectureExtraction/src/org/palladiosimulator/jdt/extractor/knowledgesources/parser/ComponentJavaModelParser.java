package org.palladiosimulator.jdt.extractor.knowledgesources.parser;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emftext.language.java.classifiers.impl.ConcreteClassifierImpl;
import org.emftext.language.java.containers.ContainersPackage;
import org.emftext.language.java.containers.impl.CompilationUnitImpl;
import org.palladiosimulator.jdt.extractor.knowledgesources.base.KnowledgeSourceBase;

import jamopp.standalone.JaMoPPStandalone;

public class ComponentJavaModelParser extends KnowledgeSourceBase {

	@Override
	public void execute() {
		
		ResourceSet resourceSetJava = new ResourceSetImpl();
		resourceSetJava.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());		
		ContainersPackage containersPackage = ContainersPackage.eINSTANCE;
		
		String partialModel = "";
		Resource componentModel = null;
				
		
		for(String component : blackboard.getDockerComponents()) {
			String buildPath = blackboard.getComponentBuildpaths().get(component);
			
			if(buildPath != null) {
				blackboard.getComponentBuildpaths().put(component, buildPath);
				partialModel = buildPath + "/" + component + ".xmi";
				JaMoPPStandalone.configure(buildPath, partialModel, true, false);
				try {
					JaMoPPStandalone.main(null);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				componentModel = resourceSetJava.getResource(URI.createFileURI(partialModel), true);
			}

						
			

			if(buildPath == null || componentModel.getContents().size() == 0) {
				System.out.println("WARNING (" + component + "): Empty Component (Couldn't find associated Java classes). Checking properties for further information...");
				
				// load config.properties
				Properties prop = new Properties();
				try {
					String config = "config.properties";
					InputStream stream = getClass().getClassLoader().getResourceAsStream(config);
					
					if(stream != null) {
						prop.load(stream);
					}
					else {
						System.out.println("Couldn't find properties file: " + config);
					}			
				} catch(Exception e) {
					System.out.println(e.getMessage());
				}
				
				// check properties
				if(prop.containsKey(component)) {
					blackboard.getMissingComponents().put(component, prop.getProperty(component));
				}
				else {
					System.out.println("Couldn't find entry for " + component + " in config.properties");
				}
			}
			else {
				for(EObject compUnit : componentModel.getContents()) {		
					if(compUnit instanceof CompilationUnitImpl) {						
						// if compilation unit contains no classes jump to next
						if(((CompilationUnitImpl) compUnit).getClassifiers().size() < 1) continue;

						ConcreteClassifierImpl concreteClass = (ConcreteClassifierImpl) ((CompilationUnitImpl) compUnit).getClassifiers().get(0);
						blackboard.getClassToComponentMap().put(concreteClass.getQualifiedName(), component);
					}
				}
			}			
		}		
	}
}
