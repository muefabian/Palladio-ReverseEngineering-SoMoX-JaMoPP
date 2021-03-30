package org.palladiosimulator.jdt.extractor.blackboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.TreeMap;
import java.util.Properties;

import org.emftext.language.java.classifiers.impl.ConcreteClassifierImpl;
import org.emftext.language.java.members.impl.MethodImpl;
import org.palladiosimulator.docker.dockerFile.impl.DockerfileImpl;
import org.palladiosimulator.jdt.extractor.knowledgesources.transformator.InterfaceTransformator;
import org.palladiosimulator.jdt.extractor.model.ExtractedComponent;
import org.palladiosimulator.jdt.extractor.model.ExtractedInterface;
import org.palladiosimulator.jdt.extractor.util.ParserType;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourceenvironment.LinkingResource;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.somox.docker.compose.composeFile.impl.DockerComposeImpl;
import org.palladiosimulator.somox.yaml.yamlFile.impl.YamlModelImpl;

import apiControlFlowInterfaces.Repo;
import factory.FluentRepositoryFactory;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;

public class Blackboard {
	
	private String projectName;
	private URI projectRoot;
	
	private InterfaceTransformator interfaceExtractor;
	
	// basic resource (java model)
	private Resource resource;	
	private DockerComposeImpl dockerComposeModel;
	
	
	private List<String> dockerComponents = new ArrayList<String>();
	private Map<String, ConcreteClassifierImpl> allClassifiers = new HashMap<>();
	private Map<String, String> classToComponentMap = new HashMap<String, String>();
	
	private Map<String, String> applicationNameToComponent = new HashMap<String, String>();
	private List<String> componentsWithExposedEndpoints = new ArrayList<>();
		
	private Map<String, String> componentBuildpaths = new HashMap<>();
	private Map<String, String> dockerBuildfilePaths = new HashMap<String,String>();
	private Map<String, DockerfileImpl> dockerBuildfiles = new HashMap<String,DockerfileImpl>();
	
	private Map<String, YamlModelImpl> springConfigBootstrapYaml = new HashMap<>();
	private Map<String, YamlModelImpl> springConfigApplicationYaml = new HashMap<>();
	private Map<String, Properties> springConfigBootstrapProperties = new HashMap<>();
	private Map<String, Properties> springConfigApplicationProperties = new HashMap<>();
	
	

	private FluentRepositoryFactory create;
	private Repo fluentRepo;
	private Repository pcmRepository;
	
	private System pcmSystem;
	
	private ResourceEnvironment pcmResEnv;
	
	private Allocation pcmAllocation;
	
	// data for ComponentExtractor
	private List<String> registeredComponents = new ArrayList<String>();
	private Map<String, ExtractedComponent> extractedComponents = new HashMap<String, ExtractedComponent>();
	private Map<String, String> missingComponents = new HashMap<String, String>();
	
	// data for InterfaceExtractor
	private Map<String, List<MethodImpl>> interfaces = new HashMap<String, List<MethodImpl>>();
	private Map<String, ExtractedInterface> extractedInterfaces = new HashMap<String, ExtractedInterface>();
	private Map<String, List<MethodImpl>> missingInterfaceMethods = new HashMap<String, List<MethodImpl>>();
	
	private Map<String, List<ConcreteClassifierImpl>> componentControllerClasses = new HashMap<>();
	private Map<String, ConcreteClassifierImpl> orphanControllerClasses = new HashMap<>();
	
	// data for DependencyExtractor
	private Map<String, List<String>> technicalDependencies = new HashMap<String, List<String>>();
	private Map<String, List<String>> logicalDependencies = new HashMap<String, List<String>>();		
	private Map<String, List<String>> methodDependencies = new HashMap<String, List<String>>();
	private List<String> genericMethodDependencies = new ArrayList<String>();	
	
	private Map<String, List<String>> missingDependencies = new HashMap<String, List<String>>();
//	private Map<String, List<MethodImpl>> missingDependencies = new HashMap<String, List<MethodImpl>>();
	
	// data for SystemExtractor
//	private Map<String, AssemblyContext> contextMap = new TreeMap<String, AssemblyContext>(String.CASE_INSENSITIVE_ORDER);
	private Map<String, AssemblyContext> contextMap = new HashMap<>();
	
	// data for ResourceExtractor
//	private Map<String, ResourceContainer> containers = new TreeMap<String, ResourceContainer>(String.CASE_INSENSITIVE_ORDER);
//	private Map<String, LinkingResource> linkingresources = new TreeMap<String, LinkingResource>(String.CASE_INSENSITIVE_ORDER);
	private Map<String, ResourceContainer> containers = new HashMap<>();
	private Map<String, LinkingResource> linkingresources = new HashMap<>();
	
	
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public URI getProjectRoot() {
		return projectRoot;
	}

	public void setProjectRoot(URI projectRoot) {
		this.projectRoot = projectRoot;
	}

	public List<String> getDockerComponents() {
		return dockerComponents;
	}

	public Map<String, ConcreteClassifierImpl> getAllClassifiers() {
		return allClassifiers;
	}

	public void addClassifier(String name, ConcreteClassifierImpl concreteClass) {
		if(!allClassifiers.containsKey(name)) {
			allClassifiers.put(name, concreteClass);
		}
	}

	public Map<String, String> getClassToComponentMap() {
		return classToComponentMap;
	}
	
	public String getComponentFromClass(String className) {
		return classToComponentMap.get(className);
	}
	
	public Map<String, String> getApplicationNameToComponent() {
		return applicationNameToComponent;
	}
	
	public String getComponentFromApplicationName(String appName) {
		return applicationNameToComponent.get(appName);
	}
	
	public List<String> getComponentsWithExposedEndpoints() {
		return componentsWithExposedEndpoints;
	}
	
	public Map<String, String> getComponentBuildpaths() {
		return componentBuildpaths;
	}
	
	public Map<String, YamlModelImpl> getSpringConfigBootstrapYaml() {
		return springConfigBootstrapYaml;
	}
	
	public Map<String, YamlModelImpl> getSpringConfigApplicationYaml() {
		return springConfigApplicationYaml;
	}
	
	public Map<String, Properties> getSpringConfigBootstrapProperties() {
		return springConfigBootstrapProperties;
	}
	
	public Map<String, Properties> getSpringConfigApplicationProperties() {
		return springConfigApplicationProperties;
	}
	
	public Map<String, String> getDockerBuildfilePaths() {
		return dockerBuildfilePaths;
	}
	
	public Map<String, DockerfileImpl> getDockerBuildfiles() {
		return dockerBuildfiles;
	}

	public Map<String, ExtractedComponent> getExtractedComponents() {
		return extractedComponents;
	}
	
	public ExtractedComponent getExtractedComponent(String name) {
		return extractedComponents.get(name);
	}
	
	public void addExtractedComponent(String name) {
		if(!extractedComponents.containsKey(name)) {
			extractedComponents.put(name, new ExtractedComponent(name));
		}
	}
	
	public Map<String, ExtractedInterface> getExtractedInterfaces() {
		return extractedInterfaces;
	}
	
	public ExtractedInterface getExtractedInterface(String name) {
		return extractedInterfaces.get(name);
	}
	
	public void addExtractedInterface(String name) {
		if(!extractedInterfaces.containsKey(name)) {
			extractedInterfaces.put(name, new ExtractedInterface("I"+name));
		}
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public DockerComposeImpl getDockerComposeModel() {
		return dockerComposeModel;
	}

	public void setDockerComposeModel(DockerComposeImpl dockerModel) {
		this.dockerComposeModel = dockerModel;
	}

	public FluentRepositoryFactory getFluentRepositoryFactory() {
		return create;
	}

	public void setFluentRepositoryFactory(FluentRepositoryFactory create) {
		this.create = create;
	}

	public Repo getFluentRepo() {
		return fluentRepo;
	}

	public void setFluentRepo(Repo fluentRepo) {
		this.fluentRepo = fluentRepo;
	}

	public Repository getPcmRepository() {
		return pcmRepository;
	}

	public void setPcmRepository(Repository pcmRepository) {
		this.pcmRepository = pcmRepository;
	}

	public System getPcmSystem() {
		return pcmSystem;
	}

	public void setPcmSystem(System system) {
		this.pcmSystem = system;
	}

	public ResourceEnvironment getPcmResourceEnvironment() {
		return pcmResEnv;
	}

	public void setPcmResourceEnvironment(ResourceEnvironment pcmResEnv) {
		this.pcmResEnv = pcmResEnv;
	}

	public Allocation getPcmAllocation() {
		return pcmAllocation;
	}

	public void setPcmAllocation(Allocation pcmAllocation) {
		this.pcmAllocation = pcmAllocation;
	}

	public List<String> getRegisteredComponents() {
		return registeredComponents;
	}
	
	public void setRegisteredComponents(List<String> registeredComponents) {
		this.registeredComponents = registeredComponents;
	}
	
	public void addRegisteredComponent(String name) {
		if(!registeredComponents.contains(name)) registeredComponents.add(name);
	}

	public Map<String, String> getMissingComponents() {
		return missingComponents;
	}

	public Map<String, List<MethodImpl>> getInterfaces() {
		return interfaces;
	}


	public Map<String, List<MethodImpl>> getMissingInterfaceMethods() {
		return missingInterfaceMethods;
	}

	public Map<String, List<ConcreteClassifierImpl>> getComponentControllerClasses() {
		return componentControllerClasses;
	}

	public void addComponentControllerClass(String component, ConcreteClassifierImpl concreteClass) {
		if(!componentControllerClasses.containsKey(component)) {
			ArrayList<ConcreteClassifierImpl> newList = new ArrayList<>();
			newList.add(concreteClass);
			componentControllerClasses.put(component, newList);
		}
		else {
			componentControllerClasses.get(component).add(concreteClass);
		}
	}
	
	public Map<String, ConcreteClassifierImpl> getOrphanControllerClasses() {
		return orphanControllerClasses;
	}

	public void addOrphanControllerClass(String qualifiedName, ConcreteClassifierImpl concreteClass) {
		if(!orphanControllerClasses.containsKey(qualifiedName)) {
			orphanControllerClasses.put(qualifiedName, concreteClass);
		}
	}

	public Map<String, List<String>> getTechnicalDependencies() {
		return technicalDependencies;
	}


	public Map<String, List<String>> getLogicalDependencies() {
		return logicalDependencies;
	}


	public Map<String, List<String>> getMethodDependencies() {
		return methodDependencies;
	}


	public List<String> getGenericMethodDependencies() {
		return genericMethodDependencies;
	}


	public Map<String, List<String>> getMissingDependencies() {
		return missingDependencies;
	}


	public Map<String, AssemblyContext> getContextMap() {
		return contextMap;
	}


	public Map<String, ResourceContainer> getContainers() {
		return containers;
	}


	public Map<String, LinkingResource> getLinkingresources() {
		return linkingresources;
	}

	public InterfaceTransformator getInterfaceExtractor() {
		return interfaceExtractor;
	}

	public void setInterfaceExtractor(InterfaceTransformator interfaceExtractor) {
		this.interfaceExtractor = interfaceExtractor;
	}

}
