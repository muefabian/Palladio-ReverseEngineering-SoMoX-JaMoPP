package org.palladiosimulator.jdt.extractor.knowledgesources.parser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emftext.language.java.containers.ContainersPackage;
import org.palladiosimulator.docker.compose.generator.ComposeFactory;
import org.palladiosimulator.jdt.extractor.knowledgesources.base.KnowledgeSourceBase;

import jamopp.standalone.JaMoPPStandalone;

public class ProjectJavaModelParser extends KnowledgeSourceBase {
	
	private List<Path> dockerfilePaths = new ArrayList<Path>();


	@Override
	public void execute() {
		
		// generate java model from project path		
		String completeModel = blackboard.getProjectRoot().toString() + "/" + blackboard.getProjectName() + "_java.xmi";
		JaMoPPStandalone.configure(blackboard.getProjectRoot().toString(), completeModel, true, true);
		try {
			JaMoPPStandalone.main(null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// load java model from xmi file and save to blackboard
		ResourceSet resourceSetJava = new ResourceSetImpl();
		resourceSetJava.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());		
		ContainersPackage containersPackage = ContainersPackage.eINSTANCE;
		Resource resourceJava = resourceSetJava.getResource(URI.createFileURI(completeModel), true);
		blackboard.setResource(resourceJava);
	}
		
		
//		// load config.properties
//		Properties prop = new Properties();
//		try {
//			String config = "config.properties";
//			InputStream stream = getClass().getClassLoader().getResourceAsStream(config);
//			
//			if(stream != null) {
//				prop.load(stream);
//			}
//			else {
//				System.out.println("Couldn't find properties file: " + config);
//			}			
//		} catch(Exception e) {
//			System.out.println(e.getMessage());
//		}
//		
//		// generate docker compose model from compose(yml) file
//		String composeFile = HelperUtil.lookupFilesWithName(java.net.URI.create(blackboard.getProjectRoot().toString()), prop.getProperty("compose")).get(0).toString();
//		String composeModel = blackboard.getProjectRoot().toString() + "/" + blackboard.getProjectName() + "_dockercompose.xmi";
//		
//		ComposeFactory.createXMI(new String[]{composeFile}, composeModel);
//		
//		// load docker compose model from xmi file and save to blackboard
//		ResourceSet resourceSetDocker = new ResourceSetImpl();
//		ComposeFileStandaloneSetup.doSetup();
//		Resource resourceDocker = resourceSetDocker.getResource(URI.createFileURI(composeModel), true);
//		DockerComposeImpl dockerModel = (DockerComposeImpl) resourceDocker.getContents().get(0);
//		blackboard.setDockerModel(dockerModel);
		

		
//		for(Service service : blackboard.getDockerComposeModel().getServices()) { 
//
//			blackboard.getDockerComponents().add(service.getName());
//		
//			Path buildPath = null;
//			String buildPathString = "";
//
//			// if the compose specifies a build parameter for this service
//			if(service.getBuild() != null) {
//
////				String build = fixPath(service.getBuild().getBuild());
//				String build = service.getBuild().getBuild().replaceAll("\"","");	
//				
//				URI buildURI = URI.createFileURI(build);					
//				buildURI = buildURI.resolve(URI.createFileURI(composeFile));
//
//				buildPath = Paths.get(java.net.URI.create(buildURI.toString()));
//				buildPathString = Paths.get(java.net.URI.create(buildURI.toString())).toString().replace("\\", "/");
//				
//				
////				blackboard.getDockerBuildfiles().put(service.getName(), buildPath.toString()+"\\Dockerfile");
//				blackboard.getDockerBuildfiles().put(service.getName(), buildPathString+"/Dockerfile");
//				
//			}
//			// if the compose specifies an image parameter for this service
//			else if(service.getImage() != null) {
//				
//				String image = service.getImage().replace("^", "").replace("\"", "");
////				if(image.equals("db")) image = "database";
//				
//				if(dockerfilePaths.size() == 0) {
//
////					java.net.URI projectURI = Paths.get("C:/Users/Fabian/git/TeastoreWithoutTests/").toUri();
////					lookupDockerfiles(projectURI);
//					dockerfilePaths = HelperUtil.lookupFilesWithName(java.net.URI.create(blackboard.getProjectRoot().toString()), "Dockerfile");
//					
////					for(Path path : dockerfilePaths) {
////						System.out.println(path.toString());
////					}
//				}
//
////				System.out.println("Image: "+image);
//
//				for(Path path : dockerfilePaths) {
//					if(path.toString().contains(image.split("-")[1]) || (image.split("-")[1].equals("db")) && path.toString().contains("database") ) {
////					if(path.toString().contains(image)) {
//						buildPath = path.getParent();
//						buildPathString = buildPath.toString().replace("\\", "/");
//						blackboard.getDockerBuildfiles().put(service.getName(), buildPathString + "/Dockerfile");
//					}
//				}
//			}
//			
//			if(buildPath != null) {
//				blackboard.getComponentBuildpaths().put(service.getName(), buildPathString);
//			}
//
////			String partialModel = "";
////			Resource componentModel = null;
//			
////			if(buildPath != null) {
////				blackboard.getComponentBuildpaths().put(service.getName(), buildPathString);
////				partialModel = buildPathString + "/" + service.getName() + ".xmi";
////				JaMoPPStandalone.configure(buildPathString, partialModel, true, false);
////				try {
////					JaMoPPStandalone.main(null);
////				} catch (Exception e) {
////					// TODO Auto-generated catch block
////					e.printStackTrace();
////				}
////				
////				componentModel = resourceSetJava.getResource(URI.createFileURI(partialModel), true);
////			}
//
////			if(buildPath == null || componentModel.getContents().size() == 0) {
////				System.out.println("WARNING (" + service.getName() + "): Empty Component (Couldn't find associated Java classes). Checking properties for further information...");
////				
////				// check properties
////				if(prop.containsKey(service.getName())) {
////					blackboard.getMissingComponents().put(service.getName(), prop.getProperty(service.getName()));
////				}
////				else {
////					System.out.println("Couldn't find entry for " + service.getName() + " in config.properties");
////				}
////			}
////			else {
////				for(EObject compUnit : componentModel.getContents()) {		
////					if(compUnit instanceof CompilationUnitImpl) {						
////						// if compilation unit contains no classes jump to next
////						if(((CompilationUnitImpl) compUnit).getClassifiers().size() < 1) continue;
////
////						ConcreteClassifierImpl concreteClass = (ConcreteClassifierImpl) ((CompilationUnitImpl) compUnit).getClassifiers().get(0);
////						blackboard.getClassToComponentMap().put(concreteClass.getQualifiedName(), service.getName());
////					}
////				}
////			}
//		}



	
//	public static void main(String[] args) {
//		
//		Initializer init = new Initializer();
//		init.blackboard = new Blackboard();
////		init.javaFile = URI.createURI("file:///C:\\\\Users\\\\Fabian\\\\git\\\\Palladio-Supporting-EclipseJavaDevelopmentTools\\\\bundles\\\\jamopp.standalone\\\\teastore_full.xmi");
////		init.dockerFile = URI.createURI("file:///C:/Users/Fabian/git/TeastoreWithoutTests/teastore.xmi");
////		init.dockerFile = URI.createURI("file:///C:/Users/Fabian/git/MicroserviceDemo/docker/microservicedemo.xmi");
//		init.execute();		
//	}
	
}
