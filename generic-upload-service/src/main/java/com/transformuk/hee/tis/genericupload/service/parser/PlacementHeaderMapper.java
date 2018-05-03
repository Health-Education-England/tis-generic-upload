package com.transformuk.hee.tis.genericupload.service.parser;

public class PlacementHeaderMapper extends ColumnMapper {
	public PlacementHeaderMapper() {
		fieldNameSource = new String[] {
				"forenames",
				"surname",
				"gmcNumber",
				"gdcNumber",
				"publicHealthNumber",
				"nationalPostNumber",
				"dateFrom",
				"dateTo",
				"placementType",
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
				"Forenames*",
				"Surname*",
				"GMC Number",
				"GDC Number",
				"Public Health Number",
				"National Post Number*",
				"Date From*",
				"Date To*",
				"Placement Type*",
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
