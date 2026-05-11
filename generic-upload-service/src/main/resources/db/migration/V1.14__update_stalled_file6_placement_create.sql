-- Set stalled file to PENDING for placement create file which is in progress
-- but has not been processed by TCS
UPDATE `ApplicationType`
SET `status` = 'PENDING'
WHERE `id` IN(49762)
  AND `logId` IN(1778320831281)
  AND `fileName` = 'TIS Placement Import Template Sheffield Aug-26.xlsx'
  AND `status` = 'IN_PROGRESS';
