package org.palladiosimulator.jdt.extractor.knowledgesources.generator;

import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.allocation.AllocationFactory;
import org.palladiosimulator.somox.docker.compose.composeFile.Service;
import org.palladiosimulator.jdt.extractor.knowledgesources.base.KnowledgeSourceBase;


// uses Docker Compose Model, Context from System model and Containers from Resource Env. Model to create PCM Allocation Model and saves it to blackboard
public class PcmAllocationGenerator extends KnowledgeSourceBase {


	@Override
	public void execute() {

		Allocation alloc = AllocationFactory.eINSTANCE.createAllocation();
		alloc.setEntityName("Default Allocation");
		
		alloc.setSystem_Allocation(blackboard.getPcmSystem());
		alloc.setTargetResourceEnvironment_Allocation(blackboard.getPcmResourceEnvironment());
		
		completeContexts(alloc);
		
		blackboard.setPcmAllocation(alloc);
		
	}
	
	private void completeContexts(Allocation alloc) {
		
		for(Service service : blackboard.getDockerComposeModel().getServices()) {
			
			String name = service.getName();
			
			AllocationContext aContext = AllocationFactory.eINSTANCE.createAllocationContext();
			aContext.setEntityName(name);
			aContext.setAllocation_AllocationContext(alloc);				
			
			aContext.setAssemblyContext_AllocationContext(blackboard.getContextMap().get(name));
			aContext.setResourceContainer_AllocationContext(blackboard.getContainers().get(name));			
						
			alloc.getAllocationContexts_Allocation().add(aContext);
			
			
		}
	
	}

}
