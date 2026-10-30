package com.pandora.bus.convert;

import com.pandora.AttachmentTO;
import com.pandora.exception.BusinessException;


public abstract class Converter {

	public static final  int OUTPUT_MIND_MAP_VIEWER = 1;
	
	public static final  int OUTPUT_IFRAME_VIEWER   = 3;
	
	
	public abstract int getOutputFormat();
		
	public abstract String getContentType();
	
	public abstract String[] getMimeTypeList();
	
	public abstract String[] getExtensionList();

	public abstract byte[] convert(AttachmentTO file) throws BusinessException ;
	
	


	
}
