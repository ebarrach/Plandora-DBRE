package com.pandora.delegate;

import com.pandora.AdditionalFieldTO;
import com.pandora.bus.AdditionalFieldBUS;
import com.pandora.exception.BusinessException;

public class AdditionalFieldDelegate extends GeneralDelegate {

	AdditionalFieldBUS bus = new AdditionalFieldBUS();

	public void updateAdditionalFieldByMetaField(AdditionalFieldTO afto) throws BusinessException {

		bus.updateAdditionalFieldByMetaField(afto);

	}

}
