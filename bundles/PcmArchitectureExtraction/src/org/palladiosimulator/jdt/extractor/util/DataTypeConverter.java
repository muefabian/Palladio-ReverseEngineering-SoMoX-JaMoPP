package org.palladiosimulator.jdt.extractor.util;

import org.emftext.language.java.classifiers.impl.ClassifierImpl;
import org.emftext.language.java.generics.impl.QualifiedTypeArgumentImpl;
import org.emftext.language.java.types.TypeReference;
import org.emftext.language.java.types.impl.BooleanImpl;
import org.emftext.language.java.types.impl.ByteImpl;
import org.emftext.language.java.types.impl.CharImpl;
import org.emftext.language.java.types.impl.ClassifierReferenceImpl;
import org.emftext.language.java.types.impl.DoubleImpl;
import org.emftext.language.java.types.impl.IntImpl;
import org.emftext.language.java.types.impl.LongImpl;
import org.emftext.language.java.types.impl.VoidImpl;
import org.palladiosimulator.jdt.extractor.blackboard.Blackboard;
import org.palladiosimulator.pcm.repository.CollectionDataType;
import org.palladiosimulator.pcm.repository.CompositeDataType;
import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.PrimitiveTypeEnum;
import org.palladiosimulator.pcm.repository.RepositoryFactory;

import apiControlFlowInterfaces.Repo;
import factory.FluentRepositoryFactory;
import repositoryStructure.internals.Primitive;
import repositoryStructure.types.CompositeDataTypeCreator;

public class DataTypeConverter {
	
	public static DataType getDataType(TypeReference typeRef, Blackboard blackboard) {
		
		if(typeRef instanceof BooleanImpl) {
			return blackboard.getFluentRepositoryFactory().fetchOfDataType(Primitive.BOOLEAN);
		}
		else if(typeRef instanceof ByteImpl) {
			return blackboard.getFluentRepositoryFactory().fetchOfDataType(Primitive.BYTE);
		}
		else if(typeRef instanceof IntImpl) {
			return blackboard.getFluentRepositoryFactory().fetchOfDataType(Primitive.INTEGER);
		}
		else if(typeRef instanceof DoubleImpl) {
			return blackboard.getFluentRepositoryFactory().fetchOfDataType(Primitive.DOUBLE);
		}
		else if(typeRef instanceof CharImpl) {
			return blackboard.getFluentRepositoryFactory().fetchOfDataType(Primitive.CHAR);
		}
		else if(typeRef instanceof LongImpl) {
			
//			RepositoryFactory.eINSTANCE.createPrimitiveDataType().setType(PrimitiveTypeEnum.LONG);
//			return blackboard.getFluentRepositoryFactory().fetchOfDataType("Long");
			
			try {
				return blackboard.getFluentRepositoryFactory().fetchOfDataType("Long");
			} catch(RuntimeException e) {
				CompositeDataTypeCreator dataType = blackboard.getFluentRepositoryFactory().newCompositeDataType().withName("Long");
				blackboard.getFluentRepo().addToRepository(dataType);
			}			
			return blackboard.getFluentRepositoryFactory().fetchOfDataType("Long");
			
		}
		else if(typeRef instanceof VoidImpl || typeRef == null) {
			try {
				return blackboard.getFluentRepositoryFactory().fetchOfDataType("Void");
			} catch(RuntimeException e) {
				CompositeDataTypeCreator dataType = blackboard.getFluentRepositoryFactory().newCompositeDataType().withName("Void");
				blackboard.getFluentRepo().addToRepository(dataType);
			}			
			return blackboard.getFluentRepositoryFactory().fetchOfDataType("Void");
		}
		
		ClassifierReferenceImpl classRef = (ClassifierReferenceImpl) typeRef;
		
		//TODO: Array
		if(classRef.getTypeArguments().size() == 1) {
			
			// List			
			QualifiedTypeArgumentImpl typeArg = (QualifiedTypeArgumentImpl) classRef.getTypeArguments().get(0);
			if(typeArg.getTypeReference().getTarget() instanceof ClassifierImpl) {
				
				ClassifierImpl classImpl = (ClassifierImpl) typeArg.getTypeReference().getTarget();				
				String name = classImpl.getName() + classRef.getTarget().getName();
				
				try {
					return blackboard.getFluentRepositoryFactory().fetchOfDataType(name);
				} catch(RuntimeException e) {
					CollectionDataType dataType = blackboard.getFluentRepositoryFactory().newCollectionDataType(name, getDataType(typeArg.getTypeReference(), blackboard));
					blackboard.getFluentRepo().addToRepository(dataType);
				}
				
				return blackboard.getFluentRepositoryFactory().fetchOfDataType(name);
			}			
		} else if(classRef.getTypeArguments().size() == 2) {
			
			//Map
			QualifiedTypeArgumentImpl typeArgOne = (QualifiedTypeArgumentImpl) classRef.getTypeArguments().get(0);
			QualifiedTypeArgumentImpl typeArgTwo = (QualifiedTypeArgumentImpl) classRef.getTypeArguments().get(1);
			
			ClassifierImpl classImplOne = (ClassifierImpl) typeArgOne.getTypeReference().getTarget();
			ClassifierImpl classImplTwo = (ClassifierImpl) typeArgTwo.getTypeReference().getTarget();
			
			String name = classImplOne.getName() + classImplTwo.getName() + classRef.getTarget().getName();
			
			try {
				return blackboard.getFluentRepositoryFactory().fetchOfDataType(name);
			} catch(RuntimeException e) {
				CompositeDataTypeCreator dataType = blackboard.getFluentRepositoryFactory().newCompositeDataType().withName(name)
						.withInnerDeclaration("Key", getDataType(typeArgOne.getTypeReference(), blackboard))
						.withInnerDeclaration("Value", getDataType(typeArgTwo.getTypeReference(), blackboard));
				blackboard.getFluentRepo().addToRepository(dataType);
			}
			
			return blackboard.getFluentRepositoryFactory().fetchOfDataType(name);			
		}
		else {
			
			// Class
			
			ClassifierImpl classifier = (ClassifierImpl) typeRef.getTarget();
			
			try {
				return blackboard.getFluentRepositoryFactory().fetchOfCompositeDataType(classifier.getName());
			} catch(RuntimeException e) {
				CompositeDataTypeCreator dataType = blackboard.getFluentRepositoryFactory().newCompositeDataType().withName(classifier.getName());
				blackboard.getFluentRepo().addToRepository(dataType);
			}
			
			return blackboard.getFluentRepositoryFactory().fetchOfCompositeDataType(classifier.getName());			
		}
		System.out.println("Error: Could not resolve data type " + classRef.getTarget().getName());
		return null;
	}
	
}