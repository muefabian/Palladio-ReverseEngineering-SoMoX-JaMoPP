package org.palladiosimulator.jdt.extractor.knowledgesources.generator;

import org.palladiosimulator.jdt.extractor.knowledgesources.base.KnowledgeSourceBase;
import org.palladiosimulator.pcm.core.composition.AssemblyConnector;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.composition.CompositionFactory;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.system.SystemFactory;


// uses Intermediate Model to create PCM System Model and saves it to blackboard
public class PcmSystemGenerator extends KnowledgeSourceBase {

	private String systemName;

	
	public void configure(String systemName) {
		this.systemName = systemName;		
	}

	@Override
	public void execute() {
		
		SystemFactory systemFactory = SystemFactory.eINSTANCE;
		System sys = systemFactory.createSystem();
		sys.setEntityName(systemName);
		
		addAssemblyContexts(sys);
		addConnectors(sys);
		
		blackboard.setPcmSystem(sys);
		
	}

	private void addAssemblyContexts(System sys) {
		
		for(String name : blackboard.getExtractedComponents().keySet()) {
						
			AssemblyContext context = CompositionFactory.eINSTANCE.createAssemblyContext();
			context.setEncapsulatedComponent__AssemblyContext(blackboard.getFluentRepositoryFactory().fetchOfComponent(name));
			context.setEntityName(name);
			sys.getAssemblyContexts__ComposedStructure().add(context);
			blackboard.getContextMap().put(name.toLowerCase(), context);
		}
	}
	
	private void addConnectors(System sys) {
		
		blackboard.getExtractedComponents().entrySet().forEach(entry -> {
			
			for(String dependency : entry.getValue().getRequires().keySet()) {
				AssemblyConnector connector = CompositionFactory.eINSTANCE.createAssemblyConnector();
				connector.setEntityName("Connector " + entry.getKey() + " -> "+dependency);
				
				connector.setRequiringAssemblyContext_AssemblyConnector(blackboard.getContextMap().get(entry.getKey()));
				connector.setProvidingAssemblyContext_AssemblyConnector(blackboard.getContextMap().get(dependency));
				
				connector.setProvidedRole_AssemblyConnector(blackboard.getFluentRepositoryFactory().fetchOfOperationProvidedRole("Provided_I"+dependency+"_"+dependency));
				connector.setRequiredRole_AssemblyConnector(blackboard.getFluentRepositoryFactory().fetchOfOperationRequiredRole("Required_I"+dependency+"_"+entry.getKey()));
				
				sys.getConnectors__ComposedStructure().add(connector);
			}			
		});

	}

}
