package com.pandora;


/**
 * 
 */
public class CustomFormTO extends PlanningTO {

	private static final long serialVersionUID = 1L;

    private MetaFormTO metaForm;
    
    private UserTO creationUser;
	
	private UserTO modificationUser;
        
    /**
     * Constructor 
     */
    public CustomFormTO(){
    }

    
    /**
     * Constructor 
     */
    public CustomFormTO(String id){
        this.setId(id);
    }    

   
    ////////////////////////////////////////
    public MetaFormTO getMetaForm() {
        return metaForm;
    }
    public void setMetaForm(MetaFormTO newValue) {
        this.metaForm = newValue;
    }


	public UserTO getCreationUser() {
		return creationUser;
	}


	public void setCreationUser(UserTO creationUser) {
		this.creationUser = creationUser;
	}


	public UserTO getModificationUser() {
		return modificationUser;
	}


	public void setModificationUser(UserTO modificationUser) {
		this.modificationUser = modificationUser;
	}
}
