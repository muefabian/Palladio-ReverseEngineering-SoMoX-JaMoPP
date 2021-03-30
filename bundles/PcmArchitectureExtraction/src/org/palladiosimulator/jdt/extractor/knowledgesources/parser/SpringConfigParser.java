package org.palladiosimulator.jdt.extractor.knowledgesources.parser;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.palladiosimulator.jdt.extractor.knowledgesources.base.KnowledgeSourceBase;
import org.palladiosimulator.jdt.extractor.util.HelperUtil;
import org.palladiosimulator.somox.xtextmodelgenerator.SpringConfigFactory;
import org.palladiosimulator.somox.yaml.YamlFileStandaloneSetup;
import org.palladiosimulator.somox.yaml.yamlFile.impl.YamlModelImpl;

public class SpringConfigParser extends KnowledgeSourceBase {

	@Override
	public void execute() {
		
		extractYamlFiles("bootstrap");
		extractYamlFiles("application");
		extractPropertiesFiles("bootstrap");
		extractPropertiesFiles("application");
		
	}

	private void extractYamlFiles(String fileName) {
		
		
		blackboard.getComponentBuildpaths().entrySet().forEach(entry -> {
			
			ResourceSet resourceSet = new ResourceSetImpl();
			YamlFileStandaloneSetup.doSetup();
			
			// parse Yaml File
			List<Path> filePath = HelperUtil.lookupFilesWithName(java.net.URI.create(entry.getValue()), fileName+".yml");
			
			if(filePath.size() > 0) {
				String input = filePath.get(0).toString().replace("\\", "/");
				String output = filePath.get(0).getParent().toString().replace("\\", "/") + "/" + fileName + "_" + entry.getKey() + ".xmi";
				System.out.println("Yaml Model created: " + output);
				SpringConfigFactory.createXMI(new String[] {input}, output);				
				
				// load Yaml File
				Resource resourceYaml = resourceSet.getResource(URI.createFileURI(output), true);
				YamlModelImpl yamlModel = (YamlModelImpl) resourceYaml.getContents().get(0);
				
				if(fileName.equals("bootstrap")) {
					blackboard.getSpringConfigBootstrapYaml().put(entry.getKey(), yamlModel);
				}
				else if(fileName.equals("application")) {
					blackboard.getSpringConfigApplicationYaml().put(entry.getKey(), yamlModel);
				}
				
				
				if(yamlModel.getSpring() != null) {
					if(yamlModel.getSpring().getApplication() != null) {
						blackboard.getApplicationNameToComponent().put(yamlModel.getSpring().getApplication().getName(), entry.getKey());
					}			
				}
			}			
		});
	}

	
	private void extractPropertiesFiles(String fileName) {
		
		blackboard.getComponentBuildpaths().entrySet().forEach(entry -> {
			
			List<Path> filePath = HelperUtil.lookupFilesWithName(java.net.URI.create(entry.getValue()), fileName+".properties");
			if(filePath.size() >0) {
				// load config.properties
				Properties prop = new Properties();
				try {
					InputStream stream = new FileInputStream(filePath.get(0).toString());
					
					if(stream != null) {
						prop.load(stream);
					}
				} catch(Exception e) {
					System.out.println(e.getMessage());
				}
				
				if(fileName.equals("bootstrap")) {
					blackboard.getSpringConfigBootstrapProperties().put(entry.getKey(), prop);
				}
				else if(fileName.equals("application")) {
					blackboard.getSpringConfigApplicationProperties().put(entry.getKey(), prop);
				}
				
				
				if(prop.containsKey("spring.application.name")) {
					blackboard.getApplicationNameToComponent().put(prop.getProperty("spring.application.name"), entry.getKey());
				}
			}			
		});
	}
	
//	public static void main(String[] args) {
//		SpringConfigurationExtractor springExt = new SpringConfigurationExtractor();
//		Blackboard bb = new Blackboard();
//		bb.getComponentBuildpaths().put("turbine", "c:/Users/Fabian/git/MicroserviceDemo/microservice-demo/microservice-demo-turbine-server");
//		springExt.configure(bb);
//		
//		springExt.execute();
//		
//		System.out.println("finish");
//	}
}
