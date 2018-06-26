package com.transformuk.hee.tis.genericupload.service.parser;

public class PlacementByIdHeaderMapper extends ColumnMapper {

	public PlacementByIdHeaderMapper() {
		fieldNameSource = new String[] {
        "placementId",
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
				"TIS_Placement_ID*",
				"National Post Number*",
				"Date From",
				"Date To",
				"Placement Type",
				"Site",
				"WTE",
				"Grade",
				"Specialty1",
				"Specialty2",
				"Specialty3",
				"Clinical Supervisor",
				"Educational Supervisor",
				"Comments"
		};
	}
}
