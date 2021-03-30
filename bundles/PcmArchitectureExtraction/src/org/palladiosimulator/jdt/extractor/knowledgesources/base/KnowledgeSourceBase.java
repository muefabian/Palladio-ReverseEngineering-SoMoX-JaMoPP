package org.palladiosimulator.jdt.extractor.knowledgesources.base;

import org.palladiosimulator.jdt.extractor.blackboard.Blackboard;
import org.palladiosimulator.jdt.extractor.util.ParserType;

public abstract class KnowledgeSourceBase implements IKnowledgeSource {

	protected Blackboard blackboard;
	protected ParserType type;
	protected boolean enabled = true;
	
	public void configure() {}
	
	public void configure(Blackboard blackboard) {
		this.blackboard = blackboard;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void configure(ParserType type) {
		this.type = type;
	}
	
	public void stop() {
		
	}
	
	public abstract void execute();
	
}
