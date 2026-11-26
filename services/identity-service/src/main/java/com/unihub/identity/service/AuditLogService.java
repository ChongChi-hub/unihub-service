package com.unihub.identity.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unihub.common.api.PageResponse;
import com.unihub.identity.domain.AuditLogEntity;
import com.unihub.identity.repository.AuditLogRepository;
import com.unihub.identity.security.GatewayUserPrincipal;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** Reusable audit logging service for tracking sensitive domain operations. */
@Service
public class AuditLogService {

	private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

	private final AuditLogRepository auditLogRepository;
	private final ObjectMapper objectMapper;

	public AuditLogService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
		this.auditLogRepository = auditLogRepository;
		this.objectMapper = objectMapper;
	}

	@Transactional
	public void record(GatewayUserPrincipal actor, String action, String targetType, String targetId, Object beforeData,
			Object afterData) {
		AuditLogEntity entry = new AuditLogEntity();
		if (actor != null) {
			entry.setActorId(actor.userId());
			entry.setActorEmail(actor.email());
		}
		entry.setAction(action);
		entry.setTargetType(targetType);
		entry.setTargetId(targetId);
		entry.setBeforeData(toJson(beforeData));
		entry.setAfterData(toJson(afterData));
		auditLogRepository.save(entry);
	}

	@Transactional(readOnly = true)
	public PageResponse<AuditLogEntity> search(String action, String targetType, String actorEmail, Pageable pageable) {
		Specification<AuditLogEntity> spec = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (StringUtils.hasText(action)) {
				predicates.add(cb.equal(root.get("action"), action.trim()));
			}
			if (StringUtils.hasText(targetType)) {
				predicates.add(cb.equal(root.get("targetType"), targetType.trim()));
			}
			if (StringUtils.hasText(actorEmail)) {
				predicates.add(cb.like(cb.lower(root.get("actorEmail")), "%" + actorEmail.trim().toLowerCase() + "%"));
			}
			return cb.and(predicates.toArray(new Predicate[0]));
		};
		Page<AuditLogEntity> page = auditLogRepository.findAll(spec, pageable);
		return PageResponse.from(page);
	}

	private String toJson(Object value) {
		if (value == null) {
			return null;
		}
		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException ex) {
			log.warn("Failed to serialize audit payload: {}", ex.getMessage());
			return null;
		}
	}
}
