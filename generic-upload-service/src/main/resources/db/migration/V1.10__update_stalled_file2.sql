-- The user asked to cancel this file
UPDATE `ApplicationType`
SET `status` = 'UNEXPECTED_ERROR'
WHERE `id` IN(32632)
  AND `logId` IN(1689773214161)
  AND `fileName` = 'Nobles - completed - 23-34.xls'
  AND `status` = 'PENDING';

-- Set stalled file to ERROR
UPDATE `ApplicationType`
SET `status` = 'UNEXPECTED_ERROR'
WHERE `id` IN(32630)
  AND `logId` IN(1689764620741)
  AND `fileName` = 'Registration list TIS update.xls'
  AND `status` = 'IN_PROGRESS';
