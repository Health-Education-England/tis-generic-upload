package com.transformuk.hee.tis.genericupload.service.parser;

public class PlacementDeleteHeaderMapper extends ColumnMapper {
	public PlacementDeleteHeaderMapper() {
		fieldNameSource = new String[] {
				"placementId",
				"placementStatus"
		};

		fieldNameTarget = new String[] {
				"Placement Id*",
				"Placement Status*"
		};
	}
}
