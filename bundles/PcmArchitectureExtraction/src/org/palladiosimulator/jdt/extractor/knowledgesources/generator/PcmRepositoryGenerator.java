package org.palladiosimulator.jdt.extractor.knowledgesources.generator;

import org.palladiosimulator.jdt.extractor.knowledgesources.base.KnowledgeSourceBase;
import org.palladiosimulator.jdt.extractor.model.ExtractedMethod;
import org.palladiosimulator.jdt.extractor.model.ExtractedParameter;
import org.palladiosimulator.jdt.extractor.util.DataTypeConverter;
import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.ParameterModifier;
import org.palladiosimulator.pcm.repository.Repository;

import apiControlFlowInterfaces.Repo;
import factory.FluentRepositoryFactory;
import repositoryStructure.components.BasicComponentCreator;
import repositoryStructure.interfaces.OperationInterfaceCreator;
import repositoryStructure.interfaces.OperationSignatureCreator;

// uses Intermediate Model to create PCM Repository Model and saves it to blackboard
public class PcmRepositoryGenerator extends KnowledgeSourceBase {


	@Override
	public void execute() {
		
		FluentRepositoryFactory create = new FluentRepositoryFactory();
		blackboard.setFluentRepositoryFactory(create);

		Repo repo = create.newRepository();
		blackboard.setFluentRepo(repo);
		
		// create PCM interfaces from extracted interfaces
		blackboard.getExtractedInterfaces().values().forEach(extractedInterface -> {
			OperationInterfaceCreator newInterface = create.newOperationInterface().withName(extractedInterface.getName());
			
			for(ExtractedMethod method : extractedInterface.getMethods()) {
				DataType returnType = DataTypeConverter.getDataType(method.getReturnType(), blackboard);
				OperationSignatureCreator newSignature = newInterface.withOperationSignature().withName(method.getName()).withReturnType(returnType);
				
				for(ExtractedParameter param : method.getParameters()) {
					newSignature.withParameter(param.getName(), DataTypeConverter.getDataType(param.getType(), blackboard), ParameterModifier.NONE);
				}
				newSignature.createSignature();				
			}
			repo.addToRepository(newInterface);
		});
		

		
		blackboard.getExtractedComponents().values().forEach(extractedComponent -> {
			BasicComponentCreator createComponent = create.newBasicComponent().withName(extractedComponent.getName());
			
			extractedComponent.getProvides().values().forEach(extractedInterface -> {
				String providedRoleName = "Provided_" + extractedInterface.getName() + "_" + extractedComponent.getName();
				createComponent.provides(create.fetchOfOperationInterface(extractedInterface.getName()), providedRoleName);
			});
			
			extractedComponent.getRequires().values().forEach(extractedInterface -> {
				String requiredRoleName = "Required_" + extractedInterface.getName() + "_" + extractedComponent.getName();
				createComponent.requires(create.fetchOfOperationInterface(extractedInterface.getName()), requiredRoleName);
			});
			
			repo.addToRepository(createComponent);
		});
				
		Repository pcmRepo = repo.createRepositoryNow();	
		
		blackboard.setPcmRepository(pcmRepo);		
	}

}
