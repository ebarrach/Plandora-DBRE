package com.pandora.bus;

import com.pandora.AdditionalFieldTO;
import com.pandora.dao.AdditionalFieldDAO;
import com.pandora.exception.BusinessException;
import com.pandora.exception.DataAccessException;

public class AdditionalFieldBUS extends GeneralBusiness {

	AdditionalFieldDAO dao = new AdditionalFieldDAO();

	public void updateAdditionalFieldByMetaField(AdditionalFieldTO afto) throws BusinessException {
		try {
			dao.updateAdditionalFieldByMetaField(afto);
		} catch (DataAccessException e) {
			throw new BusinessException(e);
		}
	}

}
