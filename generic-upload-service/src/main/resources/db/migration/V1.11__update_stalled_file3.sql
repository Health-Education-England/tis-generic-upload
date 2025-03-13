-- Set stalled file to ERROR
UPDATE `ApplicationType`
SET `status` = 'UNEXPECTED_ERROR'
WHERE `id` IN(41619)
  AND `logId` IN(1741771586709)
  AND `fileName` = 'TIS People Update - email addresses.xls'
  AND `status` = 'IN_PROGRESS';
