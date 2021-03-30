package org.palladiosimulator.jdt.extractor.knowledgesources.base;

public interface IKnowledgeSource {
	
	public void configure();
	public void stop();
	public void execute();

}
