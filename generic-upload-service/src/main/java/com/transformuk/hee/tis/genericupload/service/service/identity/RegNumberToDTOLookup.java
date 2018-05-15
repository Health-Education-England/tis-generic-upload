package com.transformuk.hee.tis.genericupload.service.service.identity;

import com.transformuk.hee.tis.tcs.api.dto.GdcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.GmcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;

import java.util.Map;
import java.util.Optional;

public class RegNumberToDTOLookup {
	Map<String, GmcDetailsDTO> gmcDetailsMapForClinicalSupervisors;
	Map<String, GdcDetailsDTO> gdcDetailsMapForClinicalSupervisors;
	Map<String, PersonDTO> phnDetailsMapForClinicalSupervisors;

	Map<String, GmcDetailsDTO> gmcDetailsMapForEducationalSupervisors;
	Map<String, GdcDetailsDTO> gdcDetailsMapForEducationalSupervisors;
	Map<String, PersonDTO> phnDetailsMapForEducationalSupervisors;

	public void setGmcDetailsMapForClinicalSupervisors(Map<String, GmcDetailsDTO> gmcDetailsMapForClinicalSupervisors) {
		this.gmcDetailsMapForClinicalSupervisors = gmcDetailsMapForClinicalSupervisors;
	}

	public void setGdcDetailsMapForClinicalSupervisors(Map<String, GdcDetailsDTO> gdcDetailsMapForClinicalSupervisors) {
		this.gdcDetailsMapForClinicalSupervisors = gdcDetailsMapForClinicalSupervisors;
	}

	public void setPhnDetailsMapForClinicalSupervisors(Map<String, PersonDTO> phnDetailsMapForClinicalSupervisors) {
		this.phnDetailsMapForClinicalSupervisors = phnDetailsMapForClinicalSupervisors;
	}

	public void setGmcDetailsMapForEducationalSupervisors(Map<String, GmcDetailsDTO> gmcDetailsMapForEducationalSupervisors) {
		this.gmcDetailsMapForEducationalSupervisors = gmcDetailsMapForEducationalSupervisors;
	}

	public void setGdcDetailsMapForEducationalSupervisors(Map<String, GdcDetailsDTO> gdcDetailsMapForEducationalSupervisors) {
		this.gdcDetailsMapForEducationalSupervisors = gdcDetailsMapForEducationalSupervisors;
	}

	public void setPhnDetailsMapForEducationalSupervisors(Map<String, PersonDTO> phnDetailsMapForEducationalSupervisors) {
		this.phnDetailsMapForEducationalSupervisors = phnDetailsMapForEducationalSupervisors;
	}

	public Optional<RegNumberDTO> getDTOForEducationalSupervisor(String regNumber) {
		if(gmcDetailsMapForEducationalSupervisors.containsKey(regNumber)) {
			return Optional.of(new GmcDTO(gmcDetailsMapForEducationalSupervisors.get(regNumber)));
		} else if(gdcDetailsMapForEducationalSupervisors.containsKey(regNumber)) {
			return Optional.of(new GdcDTO(gdcDetailsMapForEducationalSupervisors.get(regNumber)));
		} else if(phnDetailsMapForEducationalSupervisors.containsKey(regNumber)) {
			return Optional.of(new PhnDTO(phnDetailsMapForEducationalSupervisors.get(regNumber)));
		} else {
			return Optional.empty();
		}
	}

	public Optional<RegNumberDTO> getDTOForClinicalSupervisor(String regNumber) {
		if(gmcDetailsMapForClinicalSupervisors.containsKey(regNumber)) {
			return Optional.of(new GmcDTO(gmcDetailsMapForClinicalSupervisors.get(regNumber)));
		} else if(gdcDetailsMapForClinicalSupervisors.containsKey(regNumber)) {
			return Optional.of(new GdcDTO(gdcDetailsMapForClinicalSupervisors.get(regNumber)));
		} else if(phnDetailsMapForClinicalSupervisors.containsKey(regNumber)) {
			return Optional.of(new PhnDTO(phnDetailsMapForClinicalSupervisors.get(regNumber)));
		} else {
			return Optional.empty();
		}
	}
}
