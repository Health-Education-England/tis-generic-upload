package com.transformuk.hee.tis.genericupload.service.service;

import com.transformuk.hee.tis.genericupload.api.dto.PlacementDeleteXLS;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class PlacementDeleteService {
	private static final Logger logger = getLogger(PlacementDeleteService.class);

	@Autowired
	private TcsServiceImpl tcsServiceImpl;

	void processPlacementsDeleteUpload(List<PlacementDeleteXLS> placementDeleteXLSS) {
		placementDeleteXLSS.forEach(placementDeleteXLS -> {
			if ("DELETE".equalsIgnoreCase(placementDeleteXLS.getPlacementStatus())) {
				try {
					tcsServiceImpl.deletePlacement(Long.valueOf(placementDeleteXLS.getPlacementId()));
					placementDeleteXLS.setSuccessfullyImported(true);
				} catch (ResourceAccessException rae) {
					new ErrorHandler().recordErrorMessageOnTemplateOrLogUnknown(placementDeleteXLS, rae);
				}
			}
		});
	}
}
