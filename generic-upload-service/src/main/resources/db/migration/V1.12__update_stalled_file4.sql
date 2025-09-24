-- Set stalled file to ERROR
UPDATE `ApplicationType`
SET `status` = 'UNEXPECTED_ERROR'
WHERE `id` IN(46483)
  AND `logId` IN(1758710940483)
  AND `fileName` = 'NHS Email update.xls'
  AND `status` = 'IN_PROGRESS';
