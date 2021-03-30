package org.palladiosimulator.jdt.extractor.knowledgesources.generator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.palladiosimulator.jdt.extractor.knowledgesources.base.KnowledgeSourceBase;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.system.System;

public class ModelSaver extends KnowledgeSourceBase {

	private String out;
	
	
	public void configure() {
	}

	@Override
	public void execute() {
		
		this.out = blackboard.getProjectRoot().toString() + "/" + blackboard.getProjectName();
		
		save(blackboard.getPcmRepository());
		save(blackboard.getPcmSystem());
		save(blackboard.getPcmResourceEnvironment());		
		save(blackboard.getPcmAllocation());
		
	}
	
	private void saveXmiResource(Resource xmiResource)
			throws IOException {
		Map<Object, Object> options = new HashMap<Object, Object>();
		options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
		xmiResource.save(options);
	}
	
	public void save(Repository repository) {
		// create
        final ResourceSet rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("repository", new XMIResourceFactoryImpl());
        
        final URI uri = URI.createFileURI(out + ".repository");
        final Resource javaResource = rs.createResource(uri);
        
		javaResource.getContents().add(repository);		
		
		try {
			saveXmiResource(javaResource);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void save(System system) {
		// create
        final ResourceSet rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("system", new XMIResourceFactoryImpl());
        
        final URI uri = URI.createFileURI(out + ".system");
        final Resource javaResource = rs.createResource(uri);
        
		javaResource.getContents().add(system);		
		
		try {
			saveXmiResource(javaResource);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void save(ResourceEnvironment resEnv) {
		// create
        final ResourceSet rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("resourceenvironment", new XMIResourceFactoryImpl());
        
        final URI uri = URI.createFileURI(out + ".resourceenvironment");
        final Resource javaResource = rs.createResource(uri);
        
		javaResource.getContents().add(resEnv);		
		
		try {
			saveXmiResource(javaResource);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void save(Allocation allocation) {
		// create
        final ResourceSet rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("allocation", new XMIResourceFactoryImpl());
        
        final URI uri = URI.createFileURI(out + ".allocation");
        final Resource javaResource = rs.createResource(uri);
        
		javaResource.getContents().add(allocation);		
		
		try {
			saveXmiResource(javaResource);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
