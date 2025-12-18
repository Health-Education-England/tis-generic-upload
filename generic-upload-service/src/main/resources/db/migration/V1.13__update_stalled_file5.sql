-- Set stalled file to ERROR
UPDATE `ApplicationType`
SET `status` = 'UNEXPECTED_ERROR'
WHERE `id` IN(47513)
  AND `logId` IN(1765981585333)
  AND `fileName` = 'TIS People Update Template (7).xls'
  AND `status` = 'IN_PROGRESS';
