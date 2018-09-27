package com.transformuk.hee.tis.genericupload.service.parser;

public class PlacementUpdateHeaderMapper extends ColumnMapper {
	public PlacementUpdateHeaderMapper() {
		fieldNameSource = new String[] {
				"surname",
				"gmcNumber",
				"gdcNumber",
				"publicHealthNumber",
				"nationalPostNumber",
				"dateFrom",
				"dateTo",
				"placementType",
				"placementStatus",
				"site",
				"wte",
				"grade",
				"specialty1",
				"specialty2",
				"specialty3",
				"clinicalSupervisor",
				"educationalSupervisor",
				"comments"
		};

		fieldNameTarget = new String[] {
				"Surname*",
				"GMC Number",
				"GDC Number",
				"Public Health Number",
				"National Post Number*",
				"Date From*",
				"Date To*",
				"Placement Type*",
				"Placement Status*",
				"Site*",
				"WTE*",
				"Grade*",
				"Specialty1",
				"Specialty2",
				"Specialty3",
				"Clinical Supervisor",
				"Educational Supervisor",
				"Comments"
		};
	}
}
