package org.palladiosimulator.jdt.extractor.controller;

import org.eclipse.emf.common.util.URI;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emftext.language.java.containers.ContainersPackage;
import org.palladiosimulator.jdt.extractor.blackboard.Blackboard;
import org.palladiosimulator.jdt.extractor.knowledgesources.base.IKnowledgeSource;
import org.palladiosimulator.jdt.extractor.knowledgesources.base.KnowledgeSourceBase;
import org.palladiosimulator.jdt.extractor.knowledgesources.generator.ModelSaver;
import org.palladiosimulator.jdt.extractor.knowledgesources.generator.PcmAllocationGenerator;
import org.palladiosimulator.jdt.extractor.knowledgesources.generator.PcmRepositoryGenerator;
import org.palladiosimulator.jdt.extractor.knowledgesources.generator.PcmResourceEnvGenerator;
import org.palladiosimulator.jdt.extractor.knowledgesources.generator.PcmSystemGenerator;
import org.palladiosimulator.jdt.extractor.knowledgesources.parser.ComponentJavaModelParser;
import org.palladiosimulator.jdt.extractor.knowledgesources.parser.DockerComposeParser;
import org.palladiosimulator.jdt.extractor.knowledgesources.parser.DockerfileParser;
import org.palladiosimulator.jdt.extractor.knowledgesources.parser.ProjectJavaModelParser;
import org.palladiosimulator.jdt.extractor.knowledgesources.parser.SpringConfigParser;
import org.palladiosimulator.jdt.extractor.knowledgesources.transformator.ComponentTransformator;
import org.palladiosimulator.jdt.extractor.knowledgesources.transformator.DependencyTransformator;
import org.palladiosimulator.jdt.extractor.knowledgesources.transformator.InterfaceTransformator;
import org.palladiosimulator.jdt.extractor.util.ParserType;
import org.palladiosimulator.somox.docker.compose.ComposeFileStandaloneSetup;
import org.palladiosimulator.somox.docker.compose.composeFile.impl.DockerComposeImpl;

public class Controller {
	
	private Blackboard blackboard;
	private Properties prop = new Properties();
	private ParserType type = null;
	
	private List<KnowledgeSourceBase> knowledgeSources = new ArrayList<KnowledgeSourceBase>();
	
	public Controller() {
		
		blackboard = new Blackboard();			
		
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
		
		blackboard.setProjectName(prop.getProperty("outputName"));
		URI projectPath = URI.createURI(prop.getProperty("projectPath"));
		blackboard.setProjectRoot(projectPath);
		
		if(prop.getProperty("parserType").equalsIgnoreCase("REST")) {
			type = ParserType.REST;
		}
		else if(prop.getProperty("parserType").equalsIgnoreCase("SPRING")) {
			type = ParserType.SPRING;
		}
	}
	
	public void start() {
		
		boolean finished = false;
		
//		while(!finished) {
//			finished=true;
			for(KnowledgeSourceBase ks : knowledgeSources) {				
				if(ks.isEnabled()) {
					ks.execute();
					finished = false;
				}
//			}
//			if(blackboard.getMissingComponents().size() == 0) finished = true;
		}
	}
	
	public void addProjectJavaModelParser() {
		ProjectJavaModelParser projectParser = new ProjectJavaModelParser();
		projectParser.configure(blackboard);
		knowledgeSources.add(projectParser);
	}
	
	public void addDockerComposeParser() {
		DockerComposeParser composeParser = new DockerComposeParser();
		composeParser.configure(blackboard);
		knowledgeSources.add(composeParser);
	}
	
	public void addComponentJavaModelParser() {
		ComponentJavaModelParser compParser = new ComponentJavaModelParser();
		compParser.configure(blackboard);
		knowledgeSources.add(compParser);
	}
	
	public void addDockerfileParser() {
		DockerfileParser dockerfileParser = new DockerfileParser();
		dockerfileParser.configure(blackboard);
		knowledgeSources.add(dockerfileParser);
	}
	
	public void addComponentTransformator() {
		ComponentTransformator ext = new ComponentTransformator();
		ext.configure(blackboard);
		ext.configure(type);		
		knowledgeSources.add(ext);
	}
	
	public void addComponentTransformator(String[] registerKeywords) {
		ComponentTransformator ext = new ComponentTransformator();
		ext.configure(blackboard);
		ext.configure(type);		
		ext.configure(Arrays.asList(registerKeywords));
		knowledgeSources.add(ext);
	}
	
	public void addInterfaceTransformator() {
		InterfaceTransformator ext = new InterfaceTransformator();
		ext.configure(blackboard);
		ext.configure(type);
		blackboard.setInterfaceExtractor(ext);
		knowledgeSources.add(ext);
	}
	
	public void addDependencyTransformator(boolean logical) {
		DependencyTransformator ext = new DependencyTransformator();
		ext.configure(blackboard);
		ext.configure(type, logical, prop.getProperty("logicalMethodNames").split(","));
		//TODO configure dependency method names
		knowledgeSources.add(ext);
	}
	
	public void addPcmRepositoryGenerator() {
		PcmRepositoryGenerator ext = new PcmRepositoryGenerator();
		ext.configure(blackboard);
		knowledgeSources.add(ext);
	}
	
	public void addPcmSystemGenerator(String systemName) {
		PcmSystemGenerator ext = new PcmSystemGenerator();
		ext.configure(blackboard);
		ext.configure(systemName);
		knowledgeSources.add(ext);
	}
	
	public void addPcmResourceEnvGenerator() {
		PcmResourceEnvGenerator ext = new PcmResourceEnvGenerator();
		ext.configure(blackboard);
		ext.configure(prop.getProperty("envLinks").split(","));
		knowledgeSources.add(ext);
	}
	
	public void addPcmAllocationGenerator() {
		PcmAllocationGenerator ext = new PcmAllocationGenerator();
		ext.configure(blackboard);
		knowledgeSources.add(ext);
	}
	
	public void addModelSaver() {
		ModelSaver modelSaver = new ModelSaver();
		modelSaver.configure(blackboard);
		knowledgeSources.add(modelSaver);
	}
	
	public void addSpringConfigParser() {
		SpringConfigParser springParser = new SpringConfigParser();
		springParser.configure(blackboard);
		knowledgeSources.add(springParser);
	}
	
	public void defaultStart() {
		
		addProjectJavaModelParser();
		addDockerComposeParser();
		addComponentJavaModelParser();
		addDockerfileParser();
		addSpringConfigParser();
		addComponentTransformator();
		addInterfaceTransformator();
		addDependencyTransformator(true);
		addPcmRepositoryGenerator();
		addPcmSystemGenerator(blackboard.getProjectName() + "_System");
		addPcmResourceEnvGenerator();
		addPcmAllocationGenerator();
		addModelSaver();
		
		start();
	}
	
	public static void main(String[] args) {
		
		Controller controller = new Controller();
		
		controller.defaultStart();
		
	}	
}
