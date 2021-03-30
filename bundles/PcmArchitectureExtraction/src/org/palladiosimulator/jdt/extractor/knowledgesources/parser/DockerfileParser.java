package org.palladiosimulator.jdt.extractor.knowledgesources.parser;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.palladiosimulator.somox.docker.DockerFileStandaloneSetup;
import org.palladiosimulator.somox.docker.dockerFile.impl.DockerfileImpl;
import org.palladiosimulator.somox.docker.dockerFile.impl.ExposeImpl;
import org.palladiosimulator.jdt.extractor.knowledgesources.base.KnowledgeSourceBase;
import org.palladiosimulator.pcm.resourceenvironment.LinkingResource;
import org.palladiosimulator.somox.docker.compose.composeFile.Service;
import org.palladiosimulator.somox.xtextmodelgenerator.DockerfileFactory;

public class DockerfileParser extends KnowledgeSourceBase {
	
	public void execute() {
		
		for(Service service : blackboard.getDockerComposeModel().getServices()) {
			
			// check if there is a Dockerfile for this service
			if(blackboard.getDockerBuildfilePaths().containsKey(service.getName())) {
				
				ResourceSet resourceSetDocker = new ResourceSetImpl();
				DockerFileStandaloneSetup.doSetup();
				
				// parse Dockerfile
				String filePath = blackboard.getDockerBuildfilePaths().get(service.getName());				
				String output = filePath + "_" + service.getName() + ".xmi";				
				DockerfileFactory.createXMI(new String[] {filePath}, output);
				
				// load Dockerfile
//				Resource resourceDocker = resourceSetDocker.getResource(URI.createURI(output), true);
				Resource resourceDocker = resourceSetDocker.getResource(URI.createFileURI(output), true);
				DockerfileImpl dockerModel = (DockerfileImpl) resourceDocker.getContents().get(0);
				
				blackboard.getDockerBuildfiles().put(service.getName(), dockerModel);
			}
		}
	}
}
