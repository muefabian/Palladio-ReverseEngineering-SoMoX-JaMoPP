package org.palladiosimulator.jdt.extractor.knowledgesources.parser;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.palladiosimulator.somox.xtextmodelgenerator.ComposeFactory;
import org.palladiosimulator.jdt.extractor.knowledgesources.base.KnowledgeSourceBase;
import org.palladiosimulator.jdt.extractor.util.HelperUtil;
import org.palladiosimulator.somox.docker.compose.ComposeFileStandaloneSetup;
import org.palladiosimulator.somox.docker.compose.composeFile.Service;
import org.palladiosimulator.somox.docker.compose.composeFile.impl.DockerComposeImpl;

// saves docker compose model, docker buildpath & buildfile path to blackboard
public class DockerComposeParser extends KnowledgeSourceBase {

	public void execute() {
		
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
				
		// generate docker compose model from compose(yml) file
		String composeFile = HelperUtil.lookupFilesWithName(java.net.URI.create(blackboard.getProjectRoot().toString()), prop.getProperty("compose")).get(0).toString();
		String composeModel = blackboard.getProjectRoot().toString() + "/" + blackboard.getProjectName() + "_dockercompose.xmi";
				
		ComposeFactory.createXMI(new String[]{composeFile}, composeModel);
				
		// load docker compose model from xmi file and save to blackboard
		ResourceSet resourceSetDocker = new ResourceSetImpl();
		ComposeFileStandaloneSetup.doSetup();
		Resource resourceDocker = resourceSetDocker.getResource(URI.createFileURI(composeModel), true);
		DockerComposeImpl dockerModel = (DockerComposeImpl) resourceDocker.getContents().get(0);
		blackboard.setDockerComposeModel(dockerModel);
		
		
		for(Service service : blackboard.getDockerComposeModel().getServices()) { 

			blackboard.getDockerComponents().add(service.getName());
		
			Path buildPath = null;
			String buildPathString = "";

			// if the compose specifies a build parameter for this service
			if(service.getBuild() != null) {

//				String build = fixPath(service.getBuild().getBuild());
				String build = service.getBuild().getBuild().replaceAll("\"","");	
				
				URI buildURI = URI.createFileURI(build);					
				buildURI = buildURI.resolve(URI.createFileURI(composeFile));

				buildPath = Paths.get(java.net.URI.create(buildURI.toString()));
				buildPathString = Paths.get(java.net.URI.create(buildURI.toString())).toString().replace("\\", "/");
				
				
//				blackboard.getDockerBuildfiles().put(service.getName(), buildPath.toString()+"\\Dockerfile");
				blackboard.getDockerBuildfilePaths().put(service.getName(), buildPathString+"/Dockerfile");
				
			}
			// if the compose specifies an image parameter for this service
			else if(service.getImage() != null) {
				
				String image = service.getImage().replace("^", "").replace("\"", "");
//				if(image.equals("db")) image = "database";
				
				List<Path> dockerfilePaths = HelperUtil.lookupFilesWithName(java.net.URI.create(blackboard.getProjectRoot().toString()), "Dockerfile");;

				for(Path path : dockerfilePaths) {
					if(path.toString().contains(image.split("-")[1]) || (image.split("-")[1].equals("db")) && path.toString().contains("database") ) {
						buildPath = path.getParent();
						buildPathString = buildPath.toString().replace("\\", "/");
						blackboard.getDockerBuildfilePaths().put(service.getName(), buildPathString + "/Dockerfile");
					}
				}
			}
			
			if(buildPath != null) {
				blackboard.getComponentBuildpaths().put(service.getName(), buildPathString);
			}
		}
	}	
}
