package uk.nhs.cdss.audit.sqs;

import uk.nhs.cdss.audit.model.AuditSession;

public interface AuditSender {
    void sendAudit(AuditSession session);
}
