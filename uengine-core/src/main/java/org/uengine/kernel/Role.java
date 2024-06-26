package org.uengine.kernel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.uengine.contexts.TextContext;
import org.uengine.modeling.ElementView;
import org.uengine.modeling.IElement;
import org.uengine.util.UEngineUtil;

/**
 * @author Jinyoung Jang
 */
public class Role implements IElement, java.io.Serializable, Cloneable {
	private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;
	
	public static final int DISPATCHINGOPTION_AUTO				= -1; // ����
	public static final int DISPATCHINGOPTION_RACING			= 1; // ����
	public static final int DISPATCHINGOPTION_LOADBALANCED 		= 2; // �ε� ���
	public static final int DISPATCHINGOPTION_ALL 				= 0; // ���
	public static final int DISPATCHINGOPTION_SETBYRIGHTPERSON 	= 4; // ����ڿ� ���� ��d
	public static final int DISPATCHINGOPTION_REFERENCE 		= 5; //����ڿ� ���� ��d
	public static final int DISPATCHINGOPTION_RECEIVE 			= 6; //����ڿ� ���� ��d
	public static final int DISPATCHINGOPTION_ANNOUNCEMENT 		= 7; //Notice Option
	
	public static final int ASSIGNTYPE_USER		 	= 0;	
	public static final int ASSIGNTYPE_DEPT 		= 2;	
	public static final int ASSIGNTYPE_GROUP 		= 3;	
	public static final int ASSIGNTYPE_ROLE 		= 4;

	public static final String WHEN_ADD = "add";

//	String uuid;
//		@Face(displayName = "", ejsPath="dwr/metaworks/org/uengine/kernel/UUID.ejs", options = {"style"}, values = {"display:none"})
//		public String getUuid() {
//			return uuid;
//		}
//		public void setUuid(String uuid) {
//			this.uuid = uuid;
//		}


	private java.lang.String name;
	// @Name
	// @Face(displayName="역할 이름")
	// @Order(1)
		public String getName() {
			return name;
		}
		public void setName(String value) {
			name = value;
		}

	private RoleResolutionContext roleResolutionContext = null;
	// @Hidden
	// @Face(displayName="역할 선택")
	// @Order(3)
		public RoleResolutionContext getRoleResolutionContext() {
			if (roleResolutionContext instanceof DirectRoleResolutionContext) {
				DirectRoleResolutionContext drrc = (DirectRoleResolutionContext) roleResolutionContext;
				if(!UEngineUtil.isNotEmpty(drrc.getEndpoint())) {
					roleResolutionContext = null;
				}
			}
			if( roleResolutionContext == null ){
				String roleresolutioncontexts = GlobalContext.getPropertyString("roleresolutioncontexts", null);
				if( roleresolutioncontexts != null ){
				}
			}
			
			return roleResolutionContext;
		}
		public void setRoleResolutionContext(RoleResolutionContext context) {
			roleResolutionContext = context;

			if(context!=null){
				setAskWhenInit(false);
			}
		}
	
	int dispatchingOption = DISPATCHINGOPTION_ALL;
	//@Hidden
		public int getDispatchingOption() {
			return dispatchingOption;
		}
	
		public void setDispatchingOption(int i) {
			dispatchingOption = i;
		}
	private ServiceDefinition serviceType;
	//@Hidden
		public ServiceDefinition getServiceType() {
			return serviceType;
		}
		public void setServiceType(ServiceDefinition definition) {
			serviceType = definition;
		}
	
	private boolean isHumanWorker;
	//@Hidden
		public boolean isHumanWorker() {
			return isHumanWorker;
		}	
		public void setHumanWorker(boolean b) {
			isHumanWorker = b;
		}
		
	private boolean askWhenInit = true;
	//@Hidden
		public boolean isAskWhenInit() {
			return askWhenInit;
		}
		public void setAskWhenInit(boolean b) {
			askWhenInit = b;
		}

	private ProcessVariable identifier;
	
		public ProcessVariable getIdentifier() {
			return identifier;
		}
		public void setIdentifier(ProcessVariable variable) {
			identifier = variable;
		}

	private String defaultEndpoint;
	
		public String getDefaultEndpoint() {
			return defaultEndpoint;
		}
		public void setDefaultEndpoint(String string) {
			defaultEndpoint = string;
		}
		
	private TextContext displayName = org.uengine.contexts.TextContext.createInstance();
		public TextContext getDisplayName() {
			if(displayName==null){
				displayName = TextContext.createInstance();
			}
			
			/*
			if(displayName.getText()==null)
				displayName.setText(getName());
			*/
			
			return displayName;
		}
		public void setDisplayName(TextContext string) {
			displayName = string;
		}
		public void setDisplayName(String string) {
			displayName.setText(string);
		}

	boolean dontPersistResolutionResult;
		public boolean isDontPersistResolutionResult() {
			return dontPersistResolutionResult;
		}
		public void setDontPersistResolutionResult(boolean dontPersistResolutionResult) {
			this.dontPersistResolutionResult = dontPersistResolutionResult;
		}
	public Role(){
		this.setDisplayName("");
//		this.setUuid(UUID.randomUUID().toString());
	}

	public Role(String name){
		this. name = name;
		setDisplayName(name);
	}
	
	public RoleMapping getMapping(ProcessInstance inst) throws Exception{
		return getMapping(inst, (String)null);
	}

	public RoleMapping getMapping(ProcessInstance inst, Activity activity) throws Exception{
		return getMapping(inst, activity.getTracingTag());
	}
		
	public RoleMapping getMapping(ProcessInstance inst, String tracingTag) throws Exception{
		RoleMapping mapping = null;
		Role role = null;
		ProcessDefinition definition = null;
	
		if(inst!=null) {
			mapping = inst.getRoleMapping(getName());
			
			if (inst.getProcessDefinition()!=null) {
				definition = inst.getProcessDefinition();
				role = definition.getRole(getName());
			}
		}
		
		if(role==null)
			role = this;
			
		//clean up the existing resolution result when isDontPersistResolutionResult() option is true
		if(mapping!=null && isDontPersistResolutionResult()){
			mapping=null;
		}
		
		//TODO: is it hard-code?
		if(mapping==null) {
			//try to use role resolution context		
			Exception resolutionException = null;
			if (role.getRoleResolutionContext()!=null) {
				try{
					mapping = role.getRoleResolutionContext().getActualMapping(definition, inst, tracingTag, new java.util.Hashtable()); // danger roop with DefaultCompanyRoleResolutionContext.java (line 64) 
				}catch(Exception e){
					resolutionException = e;
					e.printStackTrace();
				}
				
				//set the rolemapping if resolution succeed. 
				//this should work for the case 'Racing' work-distribution option for future reference of the resolved mapping.
				if(!isDontPersistResolutionResult() && mapping!=null && inst != null ){
					inst.putRoleMapping(getName(), mapping);
				}
			}
			
			//try to use default finally
			if(mapping==null && ( role.getDefaultEndpoint()!=null && !"".equals(role.getDefaultEndpoint())) ){
				try{
					mapping = RoleMapping.create();
					mapping.setName(role.getName());
					mapping.setEndpoint(role.getDefaultEndpoint());
					
					if(role.isHumanWorker())
						mapping.fill(inst);
				}catch(Exception e){
					
					throw new UEngineException("Can't find user where the id ["+ role.getDefaultEndpoint()+ "] since: " + e.getMessage() + "\n Please contact to the process administrator.");
				}
			}else if(resolutionException!=null){
				throw resolutionException;
			}
		}
		
		if(mapping!=null){
			mapping.setCursor(0);
			
			do {
				RoleMapping rm = mapping.getCurrentRoleMapping();
				rm.setDispatchingOption(role.getDispatchingOption());
				mapping.replaceCurrentRoleMapping(rm);
				
			} while (mapping.next());
			
			mapping.setCursor(0);
		}
		
		return mapping;
	}
	
	public static Role forName(String name){
		return forName(name, null);
	}
	
	public static Role forName(String name, String defaultEP){	
		//review: fly-weight pattern needed
		Role role = new Role(name);
		role.setDefaultEndpoint(defaultEP);
		
		return role;
	}
	
	public boolean equals(Object obj){
		try{
			return getName().equals(((Role)obj).getName());
		}catch(Exception e){
			return false;
		}
	}
	
	public boolean containsMapping(ProcessInstance instance, RoleMapping testingRoleMapping) throws Exception{
		RoleMapping thisRoleMapping = getMapping(instance);
		
		//���� �� ������ �μ��� ���� �μ��� ���ٸ� ����8�� ��		
		if(thisRoleMapping.getAssignType() == ASSIGNTYPE_DEPT){
			if(thisRoleMapping.getGroupId().equals(testingRoleMapping.getGroupId()))
				return true;
			else
				return false;
		}

		do{
			if(testingRoleMapping.getEndpoint().equals(thisRoleMapping.getEndpoint())){
				return true;
			}
		}while(thisRoleMapping.next());
		
		return false;
	}
	
	

//end

	public String toString() {
		String dispName = getDisplayName().toString(); 
		
		if(dispName!=null)
			return dispName;
		
		return super.toString();
	}
	
	public Object clone(){
		//TODO [tuning point]: Object cloning with serialization. it will be called by ProcessManagerBean.getProcessDefintionXX method.
		try{
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			ObjectOutputStream ow = new ObjectOutputStream(bao);
			ow.writeObject(this);
			ByteArrayInputStream bio = new ByteArrayInputStream(bao.toByteArray());			
			ObjectInputStream oi = new ObjectInputStream(bio);
			
			Role clonedOne = (Role) oi.readObject();
			clonedOne.setIdentifier(null);
			clonedOne.setServiceType(null);
			clonedOne.setRoleResolutionContext(null);
			
			return clonedOne;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	transient String currentEditorId;
		public String getCurrentEditorId() {
			return currentEditorId;
		}
		public void setCurrentEditorId(String currentEditorId) {
			this.currentEditorId = currentEditorId;
		}

	/**
	 * 나중에 apply 버튼은 ActivityWindow 로 빼야한다... 지금은 텝에 버튼이 보이질 않아서 임시로 달아놓음
	 * @return
	 */
	public Object[] apply(){
		return null;
	}

	

	ElementView elementView;
	
	public ElementView createView(){
		ElementView elementView = (ElementView) UEngineUtil.getComponentByEscalation(getClass(), "view");
		elementView.setElement(this);

		return elementView;
	}
	public ElementView getElementView() {
		return this.elementView;
	}
	public void setElementView(ElementView elementView) {
		this.elementView = elementView;
	}

	public void createDocument() {
		// TODO Auto-generated method stub
	}
	@Override
	public String getDescription() {
		return this.displayName.getText();
	}

//	@ServiceMethod(payload = {"uuid"})
//	public void delete(@AutowiredFromClient RolePanel rolePanel){
//		for(Role role : rolePanel.getRoleList()){
//			if(Objects.equals(this.getUuid(), role.getUuid())){
//				rolePanel.getRoleList().remove(role);
//				break;
//			}
//		}
//		wrapReturn(rolePanel);
//	}

}
