package uk.nhs.cdss.audit;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.nhs.cactus.common.security.TokenAuthenticationService;
import uk.nhs.cdss.audit.model.AuditSession;

@Service
@Slf4j
@RequiredArgsConstructor
public class SQSService {

  private static final String SENDER = "sender";
  private static final String STRING = "String";
  private static final String SUPPLIER = "supplierId";

  @Value("${sqs.audit.queue}")
  private String loggingQueue;

  @Value("${service.name}")
  private String serviceName;

  @Qualifier("enhanced")
  private final ObjectMapper mapper;
  private final AmazonSQS sqsClient;
  private final TokenAuthenticationService authenticationService;

  public void sendAudit(AuditSession session) {

    var supplierId = authenticationService.requireSupplierId();
    if (StringUtils.isEmpty(loggingQueue)) {
      // Nowhere to send audits, log to console for now
      log.info(session.toString());
      return;
    }
    try {
      SendMessageRequest request = new SendMessageRequest()
          .withMessageGroupId(supplierId)
          .withMessageDeduplicationId(UUID.randomUUID().toString())
          .addMessageAttributesEntry(SENDER, new MessageAttributeValue()
              .withDataType(STRING)
              .withStringValue(serviceName))
          .addMessageAttributesEntry(SUPPLIER, new MessageAttributeValue()
              .withDataType(STRING)
              .withStringValue(supplierId))
          .withQueueUrl(loggingQueue)
          .withMessageBody(mapper.writeValueAsString(session));
      sqsClient.sendMessage(request);
    } catch (Exception e) {
      log.error("an error occurred sending audit session {} to SQS", session, e);
    }
  }

}
