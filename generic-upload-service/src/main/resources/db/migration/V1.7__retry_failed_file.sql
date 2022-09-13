UPDATE `ApplicationType`
SET `status` = 'PENDING'
WHERE `id` IN(23484, 23511)
  AND `logId` IN(1663086785784, 1663079220335)
  AND `fileName` = 'TIS People Update Template (5).xls'
  AND `status` = 'IN_PROGRESS';
