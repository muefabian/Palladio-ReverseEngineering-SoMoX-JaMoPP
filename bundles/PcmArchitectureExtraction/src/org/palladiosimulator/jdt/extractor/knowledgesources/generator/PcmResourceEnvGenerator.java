package org.palladiosimulator.jdt.extractor.knowledgesources.generator;

import org.eclipse.emf.common.util.EList;
import org.palladiosimulator.docker.dockerFile.impl.DockerfileImpl;
import org.palladiosimulator.docker.dockerFile.impl.ExposeImpl;
import org.palladiosimulator.jdt.extractor.knowledgesources.base.KnowledgeSourceBase;
import org.palladiosimulator.pcm.resourceenvironment.LinkingResource;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;
import org.palladiosimulator.somox.docker.compose.composeFile.Mapping;
import org.palladiosimulator.somox.docker.compose.composeFile.Service;
import org.palladiosimulator.somox.docker.compose.composeFile.impl.MappingWithoutPrefixAndColonImpl;

// uses Docker Compose Model and Dockerfile Model from blackboard to create PCM Resource Environment Model and saves it to blackboard
public class PcmResourceEnvGenerator extends KnowledgeSourceBase {
	
	private String[] envLinks;

	
	public void configure(String[] envLinks) {
		this.envLinks = envLinks;		
	}

	@Override
	public void execute() {
		ResourceEnvironment pcmResEnv;
		
		if(blackboard.getPcmResourceEnvironment() != null) {
			pcmResEnv = blackboard.getPcmResourceEnvironment();
		}
		else {
			pcmResEnv = ResourceenvironmentFactory.eINSTANCE.createResourceEnvironment();
			pcmResEnv.setEntityName("Docker Environment");
		}				
		
		addContainers(pcmResEnv);
		addLinkingResources(pcmResEnv, envLinks);
		
		blackboard.setPcmResourceEnvironment(pcmResEnv);
	}
	
	private void addLinkingResources(ResourceEnvironment pcmResEnv, String[] environmentLinks) {
		
		for(Service service : blackboard.getDockerComposeModel().getServices()) {			
			// check if there is a Dockerfile for this service
			if(blackboard.getDockerBuildfiles().containsKey(service.getName())) {
				
				DockerfileImpl dockerfileModel = blackboard.getDockerBuildfiles().get(service.getName());
				
				// check Dockerfile for exposed ports
				dockerfileModel.getInstructions().forEach(inst -> {
					if(inst instanceof ExposeImpl) {
						EList<Integer> portlist = ((ExposeImpl) inst).getPorts();
						for(Integer port : portlist) {
							LinkingResource linkRes = createOrGetLinkingResource(pcmResEnv, port.toString());
							if(!linkRes.getConnectedResourceContainers_LinkingResource().contains(blackboard.getContainers().get(service.getName()))) {
								linkRes.getConnectedResourceContainers_LinkingResource().add(blackboard.getContainers().get(service.getName()));
							}
						}
					}
				});
			}
			
			
			//TODO: check for links, depends_on, ... keywords	
			
			if(service.getExpose() != null) {
				service.getExpose().getList().forEach(str -> {
					LinkingResource linkRes = createOrGetLinkingResource(pcmResEnv, str);
					if(!linkRes.getConnectedResourceContainers_LinkingResource().contains(blackboard.getContainers().get(service.getName()))) {
						linkRes.getConnectedResourceContainers_LinkingResource().add(blackboard.getContainers().get(service.getName()));
					}
				});
			}
			
			// check for additional links (param environmentLinks) in environment variables
			if(service.getEnvironment() != null) {
				Mapping map = service.getEnvironment();
				map.getMap().forEach(e -> {
					for(String link : environmentLinks) {
						if(((MappingWithoutPrefixAndColonImpl) e).getName().equalsIgnoreCase(link)) {
							String name = ((MappingWithoutPrefixAndColonImpl) e).getValue().replace("\"", "");
							
							LinkingResource linkRes = createOrGetLinkingResource(pcmResEnv, link);
							
							if(!linkRes.getConnectedResourceContainers_LinkingResource().contains(blackboard.getContainers().get(service.getName()))) {
								linkRes.getConnectedResourceContainers_LinkingResource().add(blackboard.getContainers().get(service.getName()));
							}
							if(!linkRes.getConnectedResourceContainers_LinkingResource().contains(name)) {
								linkRes.getConnectedResourceContainers_LinkingResource().add(blackboard.getContainers().get(name));
							}
						}
					}
//					if(((MappingWithoutPrefixAndColonImpl) e).getName().equalsIgnoreCase("registry_host")) {
//						String name = ((MappingWithoutPrefixAndColonImpl) e).getValue().replace("\"", "");
//						
//						LinkingResource linkRes = createOrGetLinkingResource("Registry_Link");
//						
//						if(!linkRes.getConnectedResourceContainers_LinkingResource().contains(containers.get(service.getName()))) {
//							linkRes.getConnectedResourceContainers_LinkingResource().add(containers.get(service.getName()));
//						}
//						if(!linkRes.getConnectedResourceContainers_LinkingResource().contains(name)) {
//							linkRes.getConnectedResourceContainers_LinkingResource().add(containers.get(name));
//						}
//					}
//					else if(((MappingWithoutPrefixAndColonImpl) e).getName().equalsIgnoreCase("db_host")) {
//						String name = ((MappingWithoutPrefixAndColonImpl) e).getValue().replace("\"", "");
//						
//						LinkingResource linkRes = createOrGetLinkingResource("Database_Link");
//						
//						if(!linkRes.getConnectedResourceContainers_LinkingResource().contains(containers.get(service.getName()))) {
//							linkRes.getConnectedResourceContainers_LinkingResource().add(containers.get(service.getName()));
//						}
//						if(!linkRes.getConnectedResourceContainers_LinkingResource().contains(name)) {
//							linkRes.getConnectedResourceContainers_LinkingResource().add(containers.get(name));
//						}
//					}
				});
			}
		}
	}
	
	private LinkingResource createOrGetLinkingResource(ResourceEnvironment pcmResEnv, String name) {
		if(!blackboard.getLinkingresources().containsKey(name)) {
			
			LinkingResource newLinkingResource = ResourceenvironmentFactory.eINSTANCE.createLinkingResource();			
			newLinkingResource.setEntityName(name);
			newLinkingResource.setResourceEnvironment_LinkingResource(pcmResEnv);
			pcmResEnv.getLinkingResources__ResourceEnvironment().add(newLinkingResource);
						
			blackboard.getLinkingresources().put(name, newLinkingResource);
		}
		return blackboard.getLinkingresources().get(name);
	}
	
	private void addContainers(ResourceEnvironment pcmResEnv) {

		for(Service service : blackboard.getDockerComposeModel().getServices()) {
			createResourceContainer(pcmResEnv, service.getName());
		}
	}
	
	private void createResourceContainer(ResourceEnvironment pcmResEnv, String name) {
		if(!blackboard.getContainers().containsKey(name)) {
			ResourceContainer newResContainer = ResourceenvironmentFactory.eINSTANCE.createResourceContainer();
			newResContainer.setResourceEnvironment_ResourceContainer(pcmResEnv);
			newResContainer.setEntityName(name);
			pcmResEnv.getResourceContainer_ResourceEnvironment().add(newResContainer);
			blackboard.getContainers().put(name, newResContainer);
		}		
	}

}
