package org.uengine.kernel;

import org.uengine.kernel.GlobalContext;


/**
 * @author Jinyoung Jang
 */
public class Or extends And{
	private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;
	
	public Or(){
		this(null);
	}

	public Or( Condition first, Condition second, String description){
		//this.first = first;
		//this.second = second;
		this( new Condition[] { first, second});
		getDescription().setText(description);
	}
	
	public Or( Condition[] conditions){
		super( conditions);
	}

	public boolean isMet(ProcessInstance instance, String scope) throws Exception{
		
		Condition[] condis = getConditions();
//		boolean matched = false;
		if(condis == null || condis.length==0 )
			return true;

		for( int i=0; i< condis.length; i++){
		
			if( condis[i].isMet( instance, scope))
				return true;
		}
			
		return false;
	}


	@Override
	public String toString() {
		//return "Or";
		return super.toString("Or");
	}
}