package com.transformuk.hee.tis.genericupload.service.parser;

public class PostUpdateHeaderMapper extends ColumnMapper {
  public PostUpdateHeaderMapper(){
    fieldNameSource = new String[] {
      "postTISTd",
      "approvedGrade",
      "otherGrades",
      "specialty",
      "otherSpecialties",
      "subSpecialties",
      "trainingDescription",
      "mainSite",
      "otherSites",
      "trainingBody",
      "employingBody",
      "programmeName",
      "programmeNo",
      "owner",
      "rotation",
      "status",
      "oldPost",
      "fundingType",
      "fundingTypeOther",
      "fundingBody",
      "dateFrom",
      "dateTo"
    };
    fieldNameTarget = new String[]{
      "TIS_Post_ID*",
      "Approved grade",
      "Other grades",
      "Specialty",
      "Other specialties",
      "Sub specialties",
      "Training description",
      "Main site",
      "Other sites",
      "Training body",
      "Employing body",
      "Programme name",
      "Programme number",
      "Owner",
      "Rotation",
      "Status",
      "Old Post",
      "Funding type",
      "Funding type - If 'Other' please specify",
      "Funding Body",
      "Date From",
      "Date to"
    };
  }
}
